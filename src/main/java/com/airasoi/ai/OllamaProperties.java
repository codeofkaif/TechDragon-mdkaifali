package com.airasoi.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ai.ollama")
public record OllamaProperties(
        String baseUrl,
        String model,
        Double temperature
) {}

