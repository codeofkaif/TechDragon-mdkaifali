package com.airasoi.dto;

import java.time.Instant;
import java.util.List;

public record GenerateDishesResponse(
        Instant generatedAt,
        List<DishDto> dishes
) {}

