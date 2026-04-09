package com.airasoi.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record GenerateDishesRequest(
        @NotNull
        @NotEmpty
        @Size(max = 30)
        List<@NotNull @Size(min = 1, max = 50) String> ingredients,

        @Size(max = 30)
        String cuisine,

        @Min(1)
        @Max(20)
        Integer count
) {
    public int resolvedCount() {
        return count == null ? 5 : count;
    }
}

