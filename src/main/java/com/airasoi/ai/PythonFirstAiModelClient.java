package com.airasoi.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * Integration layer: tries the Python FastAPI service first, then the existing Ollama client.
 */
@Component
@Primary
public class PythonFirstAiModelClient implements AiModelClient {
    private static final Logger log = LoggerFactory.getLogger(PythonFirstAiModelClient.class);

    private final PythonAiClient pythonAiClient;
    private final ObjectProvider<OllamaAiModelClient> ollamaClient;

    public PythonFirstAiModelClient(
            PythonAiClient pythonAiClient,
            ObjectProvider<OllamaAiModelClient> ollamaClient
    ) {
        this.pythonAiClient = pythonAiClient;
        this.ollamaClient = ollamaClient;
    }

    @Override
    public String generate(String prompt) {
        try {
            return pythonAiClient.generate(prompt);
        } catch (Exception e) {
            OllamaAiModelClient ollama = ollamaClient.getIfAvailable();
            if (ollama == null) {
                log.warn("Python AI failed and no Ollama bean is available (check ai.provider): {}", e.getMessage());
                throw e;
            }
            log.warn("Python AI unavailable or failed ({}), using Ollama fallback", e.getMessage());
            return ollama.generate(prompt);
        }
    }
}
