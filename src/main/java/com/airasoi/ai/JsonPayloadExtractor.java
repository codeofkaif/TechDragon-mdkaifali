package com.airasoi.ai;

public final class JsonPayloadExtractor {
    private JsonPayloadExtractor() {}

    /**
     * Models sometimes wrap JSON with extra text. This extracts the first JSON array.
     */
    public static String extractFirstJsonArray(String text) {
        if (text == null) throw new IllegalArgumentException("text is null");

        int start = text.indexOf('[');
        int end = text.lastIndexOf(']');
        if (start < 0 || end < 0 || end <= start) {
            throw new IllegalArgumentException("No JSON array found in model output");
        }
        return text.substring(start, end + 1).trim();
    }

    /**
     * Models sometimes wrap JSON with extra text. This extracts the first JSON object.
     */
    public static String extractFirstJsonObject(String text) {
        if (text == null) throw new IllegalArgumentException("text is null");

        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start < 0 || end < 0 || end <= start) {
            throw new IllegalArgumentException("No JSON object found in model output");
        }
        return text.substring(start, end + 1).trim();
    }
}

