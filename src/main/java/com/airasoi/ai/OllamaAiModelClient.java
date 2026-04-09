package com.airasoi.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "ollama", matchIfMissing = true)
public class OllamaAiModelClient implements AiModelClient {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String model;
    private final double temperature;

    public OllamaAiModelClient(OllamaProperties properties, ObjectMapper objectMapper) {
        String baseUrl = (properties.baseUrl() == null || properties.baseUrl().isBlank())
                ? "http://localhost:11434"
                : properties.baseUrl().trim();
        this.model = (properties.model() == null || properties.model().isBlank())
                ? "gemma2:2b"
                : properties.model().trim();
        this.temperature = (properties.temperature() == null)
                ? 0.2d
                : properties.temperature();

        String safeBaseUrl = Objects.requireNonNull(baseUrl);
        this.restClient = RestClient.builder()
                .baseUrl(safeBaseUrl)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public String generate(String prompt) {
        // Ollama: POST /api/generate { model, prompt, stream:false }
        MediaType json = Objects.requireNonNull(MediaType.APPLICATION_JSON);
        String raw = restClient.post()
                .uri("/api/generate")
                .contentType(json)
                .body(new OllamaGenerateRequest(
                        model,
                        Objects.requireNonNull(prompt),
                        false,
                        new OllamaGenerateOptions(temperature)
                ))
                .retrieve()
                .body(String.class);

        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Empty response from Ollama");
        }

        try {
            JsonNode node = objectMapper.readTree(raw);
            JsonNode error = node.get("error");
            if (error != null && !error.isNull() && !error.asText().isBlank()) {
                throw new IllegalStateException("Ollama error: " + error.asText());
            }
            JsonNode response = node.get("response");
            if (response == null || response.isNull()) {
                throw new IllegalStateException("Missing 'response' in Ollama payload");
            }
            return Objects.toString(response.asText(), "");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse Ollama response", e);
        }
    }

    public static String buildDishesPrompt(List<String> ingredients, int countHint) {
        return AiPrompts.dishesPrompt(ingredients, countHint);
    }

    private record OllamaGenerateRequest(String model, String prompt, boolean stream, OllamaGenerateOptions options) {}
    private record OllamaGenerateOptions(double temperature) {}
}

