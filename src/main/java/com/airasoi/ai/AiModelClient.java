package com.airasoi.ai;

public interface AiModelClient {
    /**
     * Executes the model with the given prompt and returns the raw text output.
     * Services are responsible for prompting and parsing strict JSON.
     */
    String generate(String prompt);
}

