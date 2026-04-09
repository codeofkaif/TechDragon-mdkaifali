package com.airasoi.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.fastapi")
public record FastApiAiProperties(
        /** e.g. {@code http://localhost:8000} — Spring calls {@code {baseUrl}/generate?ingredients=} */
        String baseUrl,
        Integer connectTimeoutMs,
        Integer readTimeoutMs,
        /** Path on the Python service for structured recipe by ingredients (default {@code /generate}). */
        String generatePath,
        /** Retries for transient Python failures (total attempts, including the first). */
        RetryPolicy retry,
        /** Optional in-memory cache for {@code GET /generate} keyed by ingredients query. */
        CachePolicy cache
) {
    public FastApiAiProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:8000";
        } else {
            baseUrl = baseUrl.trim().replaceAll("/+$", "");
        }
        if (connectTimeoutMs == null || connectTimeoutMs < 1) {
            connectTimeoutMs = 10_000;
        }
        if (readTimeoutMs == null || readTimeoutMs < 1) {
            readTimeoutMs = 180_000;
        }
        if (generatePath == null || generatePath.isBlank()) {
            generatePath = "/generate";
        } else {
            generatePath = generatePath.trim();
            if (!generatePath.startsWith("/")) {
                generatePath = "/" + generatePath;
            }
        }
        retry = normalizeRetry(retry);
        cache = normalizeCache(cache);
    }

    private static RetryPolicy normalizeRetry(RetryPolicy r) {
        if (r == null) {
            return new RetryPolicy(3, 400L);
        }
        int attempts = r.maxAttempts() == null ? 3 : r.maxAttempts();
        if (attempts < 1) {
            attempts = 1;
        }
        if (attempts > 10) {
            attempts = 10;
        }
        long backoff = r.backoffMs() == null ? 400L : r.backoffMs();
        if (backoff < 0L) {
            backoff = 0L;
        }
        if (backoff > 30_000L) {
            backoff = 30_000L;
        }
        return new RetryPolicy(attempts, backoff);
    }

    private static CachePolicy normalizeCache(CachePolicy c) {
        if (c == null) {
            return new CachePolicy(false, 30, 500L);
        }
        boolean en = Boolean.TRUE.equals(c.enabled());
        int ttl = c.ttlMinutes() == null ? 30 : c.ttlMinutes();
        if (ttl < 1) {
            ttl = 1;
        }
        if (ttl > 24 * 60) {
            ttl = 24 * 60;
        }
        long max = c.maxEntries() == null ? 500L : c.maxEntries();
        if (max < 1L) {
            max = 1L;
        }
        if (max > 50_000L) {
            max = 50_000L;
        }
        return new CachePolicy(en, ttl, max);
    }

    public record RetryPolicy(Integer maxAttempts, Long backoffMs) {}

    public record CachePolicy(Boolean enabled, Integer ttlMinutes, Long maxEntries) {}
}
