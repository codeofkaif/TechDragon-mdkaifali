package com.airasoi.service;

import com.airasoi.ai.OllamaAiModelClient;
import com.airasoi.ai.PythonAiClient;
import com.airasoi.dto.GenerateRecipeResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * AI layer for {@code GET /recipe?ingredients=}: Python structured endpoint first,
 * then existing recipe generation (Ollama via {@link AiRecipeGeneratorService} when Python fails).
 */
@Service
public class RecipeFromIngredientsQueryService {
    private static final Logger log = LoggerFactory.getLogger(RecipeFromIngredientsQueryService.class);

    private final PythonAiClient pythonAiClient;
    private final AiRecipeGeneratorService aiRecipeGeneratorService;
    private final ObjectProvider<OllamaAiModelClient> ollamaClient;
    private final ObjectMapper objectMapper;

    public RecipeFromIngredientsQueryService(
            PythonAiClient pythonAiClient,
            AiRecipeGeneratorService aiRecipeGeneratorService,
            ObjectProvider<OllamaAiModelClient> ollamaClient,
            ObjectMapper objectMapper
    ) {
        this.pythonAiClient = pythonAiClient;
        this.aiRecipeGeneratorService = aiRecipeGeneratorService;
        this.ollamaClient = ollamaClient;
        this.objectMapper = objectMapper;
    }

    public GenerateRecipeResponse generate(String ingredientsQuery) {
        String trimmed = ingredientsQuery == null ? "" : ingredientsQuery.trim();
        if (trimmed.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ingredients query parameter is required");
        }
        if (trimmed.length() > 2000) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ingredients query is too long");
        }

        List<String> ingredientList = parseIngredientList(trimmed);
        if (ingredientList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ingredients must contain at least one item");
        }

        try {
            String rawJson = pythonAiClient.fetchStructuredRecipeByIngredients(trimmed);
            GenerateRecipeResponse mapped = mapPythonStructuredToResponse(rawJson);
            if ((mapped.recipe() == null || mapped.recipe().isBlank())
                    && (mapped.steps() == null || mapped.steps().isEmpty())) {
                throw new IllegalStateException("Python response mapped to an empty recipe");
            }
            return mapped;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Python /generate failed after retries/validation, falling back to Ollama: {}", e.getMessage());
            OllamaAiModelClient ollama = ollamaClient.getIfAvailable();
            if (ollama != null) {
                return aiRecipeGeneratorService.generateRecipeWithClient(
                        ollama,
                        "Seasonal Indian dish from your ingredients",
                        ingredientList,
                        "English",
                        null
                );
            }
            return aiRecipeGeneratorService.generateRecipe(
                    "Seasonal Indian dish from your ingredients",
                    ingredientList,
                    "English",
                    null
            );
        }
    }

    private static List<String> parseIngredientList(String q) {
        List<String> out = new ArrayList<>();
        for (String part : q.split(",")) {
            String s = part.trim();
            if (!s.isEmpty()) {
                out.add(s);
            }
            if (out.size() >= 30) {
                break;
            }
        }
        return out;
    }

    private GenerateRecipeResponse mapPythonStructuredToResponse(String rawJson) throws Exception {
        JsonNode node = objectMapper.readTree(rawJson);
        String dishName = firstNonBlank(textOf(node, "dish_name"), textOf(node, "dishName"));
        String tips = textOf(node, "tips");
        List<String> stepList = listOfText(node, "steps");
        List<String> ingList = listOfText(node, "ingredients");

        StringBuilder recipe = new StringBuilder();
        if (dishName != null && !dishName.isBlank()) {
            recipe.append(dishName.trim());
        }
        if (!ingList.isEmpty()) {
            if (recipe.length() > 0) {
                recipe.append("\n\n");
            }
            recipe.append("Ingredients: ");
            recipe.append(String.join(", ", ingList));
        }
        if (tips != null && !tips.isBlank()) {
            if (recipe.length() > 0) {
                recipe.append("\n\n");
            }
            recipe.append(tips.trim());
        }
        String recipeStr = recipe.length() > 0 ? recipe.toString() : "";

        String searchTerm = (dishName != null && !dishName.isBlank()) ? dishName.trim() + " recipe" : "Indian recipe";
        String youtubeLink = youtubeSearchLink(searchTerm);

        return new GenerateRecipeResponse(recipeStr, stepList, "", youtubeLink);
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

    private static String youtubeSearchLink(String query) {
        String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
        return "https://www.youtube.com/results?search_query=" + q;
    }

    private static String textOf(JsonNode node, String key) {
        if (node == null || key == null || !node.has(key) || node.get(key).isNull()) {
            return null;
        }
        String value = node.get(key).asText(null);
        return value == null ? null : value.trim();
    }

    private static List<String> listOfText(JsonNode node, String key) {
        if (node == null || key == null || !node.has(key) || node.get(key).isNull()) {
            return List.of();
        }
        JsonNode value = node.get(key);
        if (!value.isArray()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        for (JsonNode item : value) {
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
