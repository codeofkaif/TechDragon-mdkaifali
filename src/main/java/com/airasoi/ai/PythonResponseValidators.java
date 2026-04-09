package com.airasoi.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates JSON payloads returned by the Python FastAPI service before use downstream.
 */
public final class PythonResponseValidators {
    private PythonResponseValidators() {}

    /**
     * Validates {@code GET /generate} body: JSON object with enough content for a recipe.
     */
    public static void validateStructuredRecipeJson(String raw, ObjectMapper objectMapper) throws Exception {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Python /generate returned empty body");
        }
        JsonNode node = objectMapper.readTree(raw);
        if (!node.isObject()) {
            throw new IllegalStateException("Python /generate must return a JSON object");
        }
        String dishName = firstNonBlank(textOf(node, "dish_name"), textOf(node, "dishName"));
        List<String> steps = readStringList(node, "steps");
        List<String> ingredients = readStringList(node, "ingredients");
        String tips = textOf(node, "tips");

        boolean hasSteps = !steps.isEmpty();
        boolean hasDish = dishName != null && !dishName.isBlank();
        boolean hasIngredients = !ingredients.isEmpty();
        boolean hasTips = tips != null && !tips.isBlank();

        if (!hasSteps && !hasDish && !hasIngredients && !hasTips) {
            throw new IllegalStateException("Python /generate JSON has no dish_name, steps, ingredients, or tips");
        }
        if (!hasSteps && !hasTips && !hasDish && hasIngredients) {
            throw new IllegalStateException("Python /generate JSON missing steps and dish_name");
        }
    }

    /**
     * Validates {@code POST /v1/prompt} envelope and returns the model text.
     */
    public static String validateAndExtractPromptText(String raw, ObjectMapper objectMapper) throws Exception {
        if (raw == null || raw.isBlank()) {
            throw new IllegalStateException("Empty response from Python AI service");
        }
        JsonNode node = objectMapper.readTree(raw);
        if (!node.isObject()) {
            throw new IllegalStateException("Python /v1/prompt must return a JSON object");
        }
        JsonNode text = node.get("text");
        if (text == null || text.isNull() || !text.isTextual()) {
            throw new IllegalStateException("Missing or invalid 'text' in Python AI response");
        }
        String out = text.asText("").trim();
        if (out.isBlank()) {
            throw new IllegalStateException("Python AI 'text' field is blank");
        }
        if (out.length() > 512_000) {
            throw new IllegalStateException("Python AI 'text' exceeds maximum allowed length");
        }
        return out;
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) {
            return a;
        }
        if (b != null && !b.isBlank()) {
            return b;
        }
        return "";
    }

    private static String textOf(JsonNode node, String key) {
        if (node == null || key == null || !node.has(key) || node.get(key).isNull()) {
            return null;
        }
        String value = node.get(key).asText(null);
        return value == null ? null : value.trim();
    }

    private static List<String> readStringList(JsonNode node, String key) {
        if (node == null || key == null || !node.has(key) || node.get(key).isNull()) {
            return List.of();
        }
        JsonNode arr = node.get(key);
        if (!arr.isArray()) {
            throw new IllegalStateException("Python /generate field '" + key + "' must be an array when present");
        }
        List<String> out = new ArrayList<>();
        for (JsonNode item : arr) {
            if (item != null && item.isTextual()) {
                String s = item.asText("").trim();
                if (!s.isEmpty()) {
                    out.add(s);
                }
            }
        }
        return out;
    }
}
