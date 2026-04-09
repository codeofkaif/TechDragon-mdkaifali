package com.airasoi.service;

import com.airasoi.ai.AiModelClient;
import com.airasoi.ai.AiPrompts;
import com.airasoi.ai.JsonPayloadExtractor;
import com.airasoi.dto.GenerateRecipeResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AiRecipeGeneratorService {
    private final AiModelClient aiModelClient;
    private final ObjectMapper objectMapper;

    public AiRecipeGeneratorService(AiModelClient aiModelClient, ObjectMapper objectMapper) {
        this.aiModelClient = aiModelClient;
        this.objectMapper = objectMapper;
    }

    public GenerateRecipeResponse generateRecipe(String dishName, List<String> ingredients, String language, String note) {
        return generateRecipeWithClient(aiModelClient, dishName, ingredients, language, note);
    }

    /**
     * Runs the same recipe pipeline as {@link #generateRecipe} using the given client (e.g. Ollama-only fallback).
     */
    public GenerateRecipeResponse generateRecipeWithClient(
            AiModelClient client,
            String dishName,
            List<String> ingredients,
            String language,
            String note
    ) {
        try {
            String prompt = AiPrompts.recipePrompt(dishName, ingredients, language, note);
            String modelText = client.generate(prompt);
            try {
                return parseRecipe(modelText);
            } catch (Exception firstParseError) {
                // Retry once by asking model to convert output into strict expected JSON.
                String repairedText = client.generate(buildRepairPrompt(modelText));
                return parseRecipe(repairedText);
            }
        } catch (ResourceAccessException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "AI provider is not reachable. If using Ollama, ensure it is running and OLLAMA_BASE_URL is correct.",
                    e
            );
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    e.getMessage() != null ? e.getMessage() : "AI provider is not ready. Check AI configuration.",
                    e
            );
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "AI response was not valid JSON for recipe",
                    e
            );
        }
    }

    private GenerateRecipeResponse parseRecipe(String modelText) throws Exception {
        String jsonObject = JsonPayloadExtractor.extractFirstJsonObject(modelText);
        JsonNode node = objectMapper.readTree(jsonObject);

        // Preferred schema
        String recipe = textOf(node, "recipe");
        List<String> steps = listOfText(node, "steps");
        String time = textOf(node, "time");
        String youtubeLink = textOf(node, "youtubeLink");

        // Backward-compat schema support (recipeName/description/etc)
        if (recipe == null || recipe.isBlank()) {
            recipe = textOf(node, "description");
        }
        if ((recipe == null || recipe.isBlank()) && node.has("recipeName")) {
            recipe = "Recipe for " + textOf(node, "recipeName");
        }
        if (youtubeLink == null || youtubeLink.isBlank()) {
            youtubeLink = textOf(node, "youtube");
        }

        if (recipe == null) recipe = "";
        if (steps == null) steps = List.of();
        if (time == null) time = "";
        if (youtubeLink == null) youtubeLink = "";

        return new GenerateRecipeResponse(recipe, steps, time, youtubeLink);
    }

    private String textOf(JsonNode node, String key) {
        if (node == null || key == null || !node.has(key) || node.get(key).isNull()) return null;
        String value = node.get(key).asText(null);
        return value == null ? null : value.trim();
    }

    private List<String> listOfText(JsonNode node, String key) {
        if (node == null || key == null || !node.has(key) || node.get(key).isNull()) return List.of();
        JsonNode value = node.get(key);
        if (value.isArray()) {
            return objectMapper.convertValue(
                    value,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
        }
        if (value.isTextual()) {
            String text = value.asText("").trim();
            if (text.isEmpty()) return List.of();
            return List.of(text);
        }
        return List.of();
    }

    private String buildRepairPrompt(String rawOutput) {
        String safe = rawOutput == null ? "" : rawOutput;
        return """
                Convert the following content into ONLY valid JSON object.
                Return exactly these keys:
                - recipe (string)
                - steps (array of strings)
                - time (string)
                - youtubeLink (string)

                Rules:
                - No markdown
                - No explanation
                - No extra keys
                - If not possible, return:
                  {"recipe":"","steps":[],"time":"","youtubeLink":""}

                Content:
                %s
                """.formatted(safe);
    }
}

