package com.airasoi.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GenerateAiDishesRequest(
        @NotNull
        @NotEmpty
        @Size(max = 30)
        List<@NotNull @Size(min = 1, max = 50) String> ingredients
) {}

