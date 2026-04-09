package com.airasoi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GenerateRecipeRequest(
        @NotBlank
        @Size(max = 120)
        String dishName,

        @NotNull
        @NotEmpty
        @Size(max = 30)
        List<@NotNull @Size(min = 1, max = 50) String> ingredients,

        @Size(max = 10)
        String language,

        @Size(max = 220)
        String note
) {}

