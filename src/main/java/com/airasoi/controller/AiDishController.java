package com.airasoi.controller;

import com.airasoi.dto.GenerateAiDishesRequest;
import com.airasoi.dto.GenerateAiDishesResponse;
import com.airasoi.service.AiDishGeneratorService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class AiDishController {
    private final AiDishGeneratorService aiDishGeneratorService;

    public AiDishController(AiDishGeneratorService aiDishGeneratorService) {
        this.aiDishGeneratorService = aiDishGeneratorService;
    }

    @PostMapping(value = "/generate-dishes/ai", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public GenerateAiDishesResponse generateAiDishes(@Valid @RequestBody GenerateAiDishesRequest request) {
        return new GenerateAiDishesResponse(Instant.now(), aiDishGeneratorService.generateDishes(request.ingredients()));
    }
}

