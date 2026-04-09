package com.airasoi.service;

import com.airasoi.ai.AiModelClient;
import com.airasoi.ai.AiPrompts;
import com.airasoi.ai.JsonPayloadExtractor;
import com.airasoi.dto.AiDishDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class AiDishGeneratorService {
    private final AiModelClient aiModelClient;
    private final ObjectMapper objectMapper;

    public AiDishGeneratorService(AiModelClient aiModelClient, ObjectMapper objectMapper) {
        this.aiModelClient = aiModelClient;
        this.objectMapper = objectMapper;
    }

    public List<AiDishDto> generateDishes(List<String> ingredients) {
        List<String> cleaned = ingredients.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .distinct()
                .toList();

        try {
            String prompt = AiPrompts.dishesPrompt(cleaned, 6);
            String modelText = aiModelClient.generate(prompt);
            try {
                return parseDishList(modelText);
            } catch (Exception firstParseError) {
                // Retry once with a strict JSON-repair instruction for non-compliant model outputs.
                String repairedText = aiModelClient.generate(buildRepairPrompt(modelText));
                return parseDishList(repairedText);
            }
        } catch (ResponseStatusException e) {
            throw e;
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "AI provider is not reachable. If using Ollama, ensure it is running and OLLAMA_BASE_URL is correct.",
                    e
            );
        } catch (IllegalStateException e) {
            // Common: Ollama model missing / Ollama returned error payload
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    e.getMessage() != null ? e.getMessage() : "AI provider is not ready. Check AI configuration.",
                    e
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI response was not valid JSON for dish list",
                    e
            );
        }
    }

    private List<AiDishDto> parseDishList(String modelText) throws Exception {
        String cleaned = stripCodeFences(modelText);

        // 0) Sometimes model returns JSON as a quoted string.
        try {
            JsonNode maybeQuoted = objectMapper.readTree(cleaned);
            if (maybeQuoted.isTextual()) {
                String inner = stripCodeFences(maybeQuoted.asText());
                return parseDishList(inner);
            }
        } catch (Exception ignored) {
            // continue
        }

        // 1) Direct parse if model already returned a clean JSON array.
        try {
            return objectMapper.readValue(cleaned, new TypeReference<List<AiDishDto>>() {});
        } catch (Exception ignored) {
            // continue with extraction fallbacks
        }

        // 2) Extract first JSON array from wrapped text.
        try {
            String jsonArray = JsonPayloadExtractor.extractFirstJsonArray(cleaned);
            return objectMapper.readValue(jsonArray, new TypeReference<List<AiDishDto>>() {});
        } catch (Exception ignored) {
            // continue with object fallback
        }

        // 3) Extract first JSON object, then support either:
        //    - single dish object
        //    - wrapper object like { "dishes": [ ... ] }
        String jsonObject = JsonPayloadExtractor.extractFirstJsonObject(cleaned);
        JsonNode node = objectMapper.readTree(jsonObject);
        if (node.isObject() && node.has("dishes") && node.get("dishes").isArray()) {
            return objectMapper.convertValue(node.get("dishes"), new TypeReference<List<AiDishDto>>() {});
        }
        if (node.isObject()) {
            Optional<JsonNode> nestedArray = findDishArray(node);
            if (nestedArray.isPresent()) {
                return objectMapper.convertValue(nestedArray.get(), new TypeReference<List<AiDishDto>>() {});
            }
            return List.of(objectMapper.treeToValue(node, AiDishDto.class));
        }

        throw new IllegalArgumentException("No parsable dish JSON found");
    }

    private String stripCodeFences(String text) {
        if (text == null) return "";
        String s = text.trim();
        s = s.replace("```json", "").replace("```JSON", "").replace("```", "").trim();
        return s;
    }

    private Optional<JsonNode> findDishArray(JsonNode node) {
        if (node == null) return Optional.empty();
        if (node.isArray() && node.size() > 0 && node.get(0).isObject()) {
            JsonNode first = node.get(0);
            if (first.has("name") && first.has("region") && first.has("type") && first.has("time") && first.has("description")) {
                return Optional.of(node);
            }
        }
        if (node.isObject()) {
            var fields = node.fields();
            while (fields.hasNext()) {
                var e = fields.next();
                Optional<JsonNode> found = findDishArray(e.getValue());
                if (found.isPresent()) return found;
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                Optional<JsonNode> found = findDishArray(child);
                if (found.isPresent()) return found;
            }
        }
        return Optional.empty();
    }

    private String buildRepairPrompt(String rawOutput) {
        String safe = rawOutput == null ? "" : rawOutput;
        return """
                Convert the following content into ONLY valid JSON.
                Output must be a JSON array of objects.
                Each object must have exactly these keys:
                name, region, type, time, description

                Rules:
                - Return only JSON array
                - No markdown
                - No explanation
                - If not enough info, return []

                Content:
                %s
                """.formatted(safe);
    }
}

