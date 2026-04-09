package com.airasoi.dto;

import java.time.Instant;
import java.util.List;

public record GenerateAiDishesResponse(
        Instant generatedAt,
        List<AiDishDto> dishes
) {}

