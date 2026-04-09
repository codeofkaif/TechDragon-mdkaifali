package com.airasoi.controller;

import com.airasoi.dto.GenerateRecipeRequest;
import com.airasoi.dto.GenerateRecipeResponse;
import com.airasoi.service.AiRecipeGeneratorService;
import com.airasoi.service.RecipeFromIngredientsQueryService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RecipeController {
    private final AiRecipeGeneratorService aiRecipeGeneratorService;
    private final RecipeFromIngredientsQueryService recipeFromIngredientsQueryService;

    public RecipeController(
            AiRecipeGeneratorService aiRecipeGeneratorService,
            RecipeFromIngredientsQueryService recipeFromIngredientsQueryService
    ) {
        this.aiRecipeGeneratorService = aiRecipeGeneratorService;
        this.recipeFromIngredientsQueryService = recipeFromIngredientsQueryService;
    }

    @GetMapping(value = "/recipe", produces = MediaType.APPLICATION_JSON_VALUE)
    public GenerateRecipeResponse recipeByIngredients(@RequestParam("ingredients") String ingredients) {
        return recipeFromIngredientsQueryService.generate(ingredients);
    }

    @PostMapping(value = "/generate-recipe", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public GenerateRecipeResponse generateRecipe(@Valid @RequestBody GenerateRecipeRequest request) {
        return aiRecipeGeneratorService.generateRecipe(request.dishName(), request.ingredients(), request.language(), request.note());
    }
}

