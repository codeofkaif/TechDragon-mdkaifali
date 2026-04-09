package com.airasoi.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Objects;

/**
 * HTTP client for the Python FastAPI layer with retries, response validation, optional caching,
 * and structured errors for upstream fallback ({@link com.airasoi.ai.PythonFirstAiModelClient}).
 */
@Component
public class PythonAiClient {
    private static final Logger log = LoggerFactory.getLogger(PythonAiClient.class);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String generatePath;
    private final FastApiAiProperties properties;
    /** Nullable when caching is disabled. */
    private final Cache<String, String> structuredRecipeCache;

    public PythonAiClient(FastApiAiProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.generatePath = properties.generatePath();
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofMillis(properties.readTimeoutMs()));

        this.restClient = RestClient.builder()
                .baseUrl(Objects.requireNonNull(properties.baseUrl()))
                .requestFactory(requestFactory)
                .build();

        if (properties.cache().enabled()) {
            this.structuredRecipeCache = Caffeine.newBuilder()
                    .maximumSize(properties.cache().maxEntries())
                    .expireAfterWrite(Duration.ofMinutes(properties.cache().ttlMinutes()))
                    .build();
        } else {
            this.structuredRecipeCache = null;
        }
    }

    /**
     * Calls Python {@code GET /generate?ingredients=} and returns validated JSON body.
     */
    public String fetchStructuredRecipeByIngredients(String ingredientsQuery) {
        String key = ingredientsQuery == null ? "" : ingredientsQuery.trim();
        if (structuredRecipeCache != null) {
            String cached = structuredRecipeCache.getIfPresent(key);
            if (cached != null) {
                try {
                    PythonResponseValidators.validateStructuredRecipeJson(cached, objectMapper);
                    log.debug("Python /generate cache hit for ingredients query");
                    return cached;
                } catch (Exception e) {
                    log.debug("Invalid cached /generate entry, evicting: {}", e.getMessage());
                    structuredRecipeCache.invalidate(key);
                }
            }
        }

        String raw = executeWithRetry("Python /generate", () -> {
                    String body = doFetchStructured(key);
                    PythonResponseValidators.validateStructuredRecipeJson(body, objectMapper);
                    return body;
                });

        if (structuredRecipeCache != null) {
            structuredRecipeCache.put(key, raw);
        }
        return raw;
    }

    /**
     * Calls Python {@code POST /v1/prompt} and returns validated model text.
     */
    public String generate(String prompt) {
        return executeWithRetry(
                "Python /v1/prompt",
                () -> {
                    String raw = doPostPrompt(Objects.requireNonNull(prompt));
                    return PythonResponseValidators.validateAndExtractPromptText(raw, objectMapper);
                });
    }

    private String doFetchStructured(String ingredientsQuery) throws Exception {
        try {
            String raw = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path(generatePath)
                            .queryParam("ingredients", ingredientsQuery)
                            .build())
                    .retrieve()
                    .body(String.class);
            if (raw == null || raw.isBlank()) {
                throw new IllegalStateException("Empty Python /generate response");
            }
            return raw;
        } catch (RestClientResponseException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to call Python /generate", e);
        }
    }

    private String doPostPrompt(String prompt) throws Exception {
        MediaType json = Objects.requireNonNull(MediaType.APPLICATION_JSON);
        try {
            String raw = restClient.post()
                    .uri("/v1/prompt")
                    .contentType(json)
                    .body(new PromptRequest(prompt))
                    .retrieve()
                    .body(String.class);
            if (raw == null || raw.isBlank()) {
                throw new IllegalStateException("Empty response from Python AI service");
            }
            return raw;
        } catch (RestClientResponseException e) {
            throw e;
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to call Python AI service", e);
        }
    }

    private String executeWithRetry(String operationLabel, RetryableCall supplier) {
        int max = properties.retry().maxAttempts();
        long backoffMs = properties.retry().backoffMs();
        Exception last = null;
        for (int attempt = 1; attempt <= max; attempt++) {
            try {
                return supplier.invoke();
            } catch (Exception e) {
                last = e;
                boolean canRetry = attempt < max && isRetryableException(e);
                if (!canRetry) {
                    throw wrapAsIllegalState(operationLabel, e);
                }
                log.warn("{}: attempt {}/{} failed — {}", operationLabel, attempt, max, e.getMessage());
                sleepQuietly(backoffMs);
            }
        }
        throw wrapAsIllegalState(operationLabel, last);
    }

    private static boolean isRetryableException(Throwable t) {
        Throwable cur = t;
        while (cur != null) {
            if (cur instanceof RestClientResponseException rce) {
                int code = rce.getStatusCode().value();
                if (code == 408 || code == 429) {
                    return true;
                }
                if (code >= 500) {
                    return true;
                }
                if (code >= 400 && code < 500) {
                    return false;
                }
            }
            if (cur instanceof ResourceAccessException) {
                return true;
            }
            if (cur instanceof java.io.IOException) {
                return true;
            }
            cur = cur.getCause();
        }
        return true;
    }

    private static RuntimeException wrapAsIllegalState(String operationLabel, Exception e) {
        if (e == null) {
            return new IllegalStateException(operationLabel + " failed after retries");
        }
        if (e instanceof IllegalStateException ise) {
            return ise;
        }
        if (e instanceof RestClientResponseException rce) {
            String body = rce.getResponseBodyAsString();
            String hint = body == null || body.isBlank() ? rce.getStatusText() : body;
            return new IllegalStateException(
                    operationLabel + " error " + rce.getStatusCode() + ": " + hint,
                    e
            );
        }
        return new IllegalStateException(operationLabel + " failed", e);
    }

    private static void sleepQuietly(long backoffMs) {
        if (backoffMs <= 0L) {
            return;
        }
        try {
            Thread.sleep(backoffMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @FunctionalInterface
    private interface RetryableCall {
        String invoke() throws Exception;
    }

    private record PromptRequest(String prompt) {}
}
