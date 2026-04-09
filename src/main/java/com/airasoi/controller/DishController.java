package com.airasoi.controller;

import com.airasoi.dto.GenerateDishesRequest;
import com.airasoi.dto.GenerateDishesResponse;
import com.airasoi.service.DishGeneratorService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class DishController {
    private final DishGeneratorService dishGeneratorService;

    public DishController(DishGeneratorService dishGeneratorService) {
        this.dishGeneratorService = dishGeneratorService;
    }

    @PostMapping(value = "/generate-dishes", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public GenerateDishesResponse generateDishes(@Valid @RequestBody GenerateDishesRequest request) {
        return new GenerateDishesResponse(Instant.now(), dishGeneratorService.generate(request));
    }
}

