package com.airasoi.dto;

import java.util.List;
import java.util.UUID;

public record DishDto(
        UUID id,
        String name,
        String cuisine,
        List<String> ingredients
) {}

