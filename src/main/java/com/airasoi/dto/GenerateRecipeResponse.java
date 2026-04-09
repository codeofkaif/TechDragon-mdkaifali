package com.airasoi.dto;

import java.util.List;

public record GenerateRecipeResponse(
        String recipe,
        List<String> steps,
        String time,
        String youtubeLink
) {}

