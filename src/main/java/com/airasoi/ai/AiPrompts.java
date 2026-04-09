// package com.airasoi.ai;

// import java.util.List;
// import java.util.Locale;

// public final class AiPrompts {
//     private AiPrompts() {}

//     public static String dishesPrompt(List<String> ingredients, int countHint) {
//         String ingredientList = ingredients.stream()
//                 .map(String::trim)
//                 .filter(s -> !s.isEmpty())
//                 .map(s -> s.toLowerCase(Locale.ROOT))
//                 .distinct()
//                 .limit(30)
//                 .reduce((a, b) -> a + ", " + b)
//                 .orElse("");

//         int n = Math.max(1, Math.min(countHint, 10));

//         return """
//                 You are a culinary assistant. Return ONLY valid JSON.
                
//                 Task: generate %d dish ideas using these ingredients: [%s]
                
//                 IMPORTANT:
//                 - Suggest ONLY Indian dishes.
//                 - region MUST be an Indian region/state/cuisine (e.g. "North Indian", "South Indian", "Punjabi", "Bihar", "Maharashtra", "Tamil Nadu", "Bengali").
                
//                 Output MUST be a JSON array. Each item MUST have exactly these keys:
//                 - name (string)
//                 - region (string)
//                 - type (string)  // e.g. Veg / Non-Veg
//                 - time (string)  // e.g. "20 min" or "Quick"
//                 - description (string)
                
//                 Rules:
//                 - Do not include markdown.
//                 - Do not include any extra keys.
//                 - Keep description to 1-2 sentences.
//                 """.formatted(n, ingredientList);
//     }

//     public static String recipePrompt(String dishName, List<String> ingredients) {
//         return recipePrompt(dishName, ingredients, null, null);
//     }

//     public static String recipePrompt(String dishName, List<String> ingredients, String language, String note) {
//         String ingredientList = ingredients.stream()
//                 .map(String::trim)
//                 .filter(s -> !s.isEmpty())
//                 .map(s -> s.toLowerCase(Locale.ROOT))
//                 .distinct()
//                 .limit(30)
//                 .reduce((a, b) -> a + ", " + b)
//                 .orElse("");

//         String lang = normalizeLanguage(language);
//         String noteLine = (note == null || note.isBlank()) ? "" : ("\nAdditional preferences: " + note.trim());

//         return """
//                 You are a helpful cooking assistant. Return ONLY valid JSON.
                
//                 Create a recipe for the dish: "%s"
//                 Use these ingredients (you may add common pantry items like salt, oil, water, spices): [%s]
//                 Write the recipe and steps in this language: %s
//                 %s
                
//                 IMPORTANT:
//                 - Recipe MUST be Indian-style (spices/tempering typical to Indian cooking).
//                 - If dish name is not Indian, adapt it into an Indian version.
                
//                 Output MUST be a JSON object with exactly these keys:
//                 - recipe (string)        // short overview of the recipe
//                 - steps (array of strings)
//                 - time (string)          // e.g. "35 min"
//                 - youtubeLink (string)   // a YouTube search URL for the dish (not a random video id)
                
//                 Rules:
//                 - Do not include markdown.
//                 - Do not include any extra keys.
//                 - Steps should be 5-10 items.
//                 """.formatted(dishName.trim(), ingredientList, lang, noteLine);
//     }

//     private static String normalizeLanguage(String language) {
//         if (language == null || language.isBlank()) return "English";
//         String s = language.trim().toLowerCase(Locale.ROOT);
//         return switch (s) {
//             case "en", "english" -> "English";
//             case "hi", "hindi" -> "Hindi";
//             case "bn", "bangla", "bengali" -> "Bengali";
//             case "ta", "tamil" -> "Tamil";
//             case "te", "telugu" -> "Telugu";
//             case "mr", "marathi" -> "Marathi";
//             default -> "English";
//         };
//     }
// }

package com.airasoi.ai;

import java.util.List;
import java.util.Locale;

public final class AiPrompts {
    private AiPrompts() {}

    // =========================
    // 🔥 DISH LIST PROMPT
    // =========================
    public static String dishesPrompt(List<String> ingredients, int countHint) {

        String ingredientList = ingredients.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .distinct()
                .limit(30)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        return """
                You are an expert Indian culinary assistant.

                TASK:
                Generate 5 to 6 Indian dish ideas using these ingredients:
                [%s]

                OUTPUT FORMAT (STRICT JSON ONLY):
                Return ONLY a JSON array.

                Each item must have EXACTLY:
                {
                  "name": "",
                  "region": "",
                  "type": "",
                  "time": "",
                  "description": ""
                }

                🔥 RULES:

                1. REGION PRIORITY:
                - At least 3 dishes MUST be from Uttar Pradesh
                - Remaining from North Indian
                - Prefer Uttar Pradesh dishes
                - fallback: North Indian

                2. INGREDIENT USAGE:
                - Use given ingredients as PRIMARY ingredients
                - You may add ONLY basic kitchen items:
                  (salt, oil, turmeric, cumin, chili powder)
                - Do NOT introduce complex ingredients
                - If dish requires extra ingredients → skip it
                - Prefer simple dishes with minimal ingredients.

                3. REALISTIC DISHES:
                - Only real Indian dishes
                - No fake names
                - Common kitchen feasible
                - Only suggest dishes that naturally match given ingredients
                - Avoid forced combinations

                4. TAG RULES:
                - region must be valid Indian cuisine
                - type must be "Veg" or "Non-Veg"
                - time must be:
                  "Quick", "Medium", "Long"

                5. DESCRIPTION:
                - Max 1 short sentence

                6. STRICT:
                - No markdown
                - No explanation
                - No extra keys
                - Valid JSON only

                7. SAFETY:
                - If not possible → return []
                - If no valid dish or recipe can be generated:
                  → return empty JSON or []
                - No exceptions
                - No invalid output
                - No extra text
                """.formatted(ingredientList);
    }


    // =========================
    // 🔥 RECIPE PROMPT
    // =========================
    public static String recipePrompt(String dishName, List<String> ingredients) {
        return recipePrompt(dishName, ingredients, null, null);
    }

    public static String recipePrompt(String dishName, List<String> ingredients, String language, String note) {

        String ingredientList = ingredients.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .distinct()
                .limit(30)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");

        String lang = normalizeLanguage(language);
        String noteLine = (note == null || note.isBlank()) ? "" : ("\nAdditional preferences: " + note.trim());

        return """
                You are a professional Indian chef.

                TASK:
                Create a complete recipe for the dish: "%s"

                USER INGREDIENTS:
                [%s]

                LANGUAGE:
                %s

                %s

                OUTPUT FORMAT (STRICT JSON ONLY):
                {
                  "recipeName": "",
                  "region": "",
                  "description": "",
                  "time": "",
                  "difficulty": "",
                  "servings": "",
                  "ingredients": {
                    "required": [],
                    "youHave": [],
                    "youMayNeed": []
                  },
                  "steps": [],
                  "youtubeLink": ""
                }

                🔥 CORE REQUIREMENTS:

                1. REGION PRIORITY:
                - Prefer dishes from Uttar Pradesh
                - Otherwise North Indian

                2. INGREDIENT LOGIC:
                - "youHave" = user ingredients
                - "required" = full ingredients used in recipe
                - "youMayNeed" = missing ingredients
                - Only allow basic pantry items:
                  (salt, oil, turmeric, cumin, chili)

                DYNAMIC INGREDIENT VALIDATION:

                - Treat user-provided ingredients as PRIMARY ingredients
                - You MUST base the dish strictly on these ingredients

                - You MAY include ONLY minimal supporting ingredients IF they are:
                  1. Universally available in Indian kitchens
                  2. Required for cooking process (not main ingredients)

                Examples of supporting ingredients:
                - salt, oil, water
                - basic spices (turmeric, cumin, chili powder)

                IMPORTANT:
                - DO NOT introduce any new vegetables, grains, or main ingredients
                - DO NOT change the core identity of the dish

                SMART CHECK:
                - If the dish requires additional major ingredients → reject it
                - Prefer dishes naturally compatible with given ingredients
                - Prefer dishes that naturally require minimal ingredients.

                SELF VALIDATION (MANDATORY):

                Before generating final output, internally verify:

                1. Ingredient Check:
                   - Are all main ingredients from user input?
                   - Are extra items only basic cooking essentials?

                2. Cooking Realism:
                   - Does the process follow real cooking flow?
                   - Is this dish actually made in real kitchens?

                3. Simplicity Check:
                   - Is the recipe feasible with given ingredients?

                If ANY check fails:
                → Regenerate a better response
                → If still not possible, return empty output

                3. REALISTIC COOKING:
                - Follow real Indian cooking flow:
                  heat oil → spices → vegetables → cook → finish
                - No unrealistic steps

                4. STEPS:
                - 6 to 10 steps
                - Each step short and practical

                5. METADATA:
                - difficulty: Easy / Medium / Hard
                - servings: e.g. "2 servings"
                - time: realistic (e.g. "20 min")

                6. DESCRIPTION:
                - One short line

                7. YOUTUBE LINK:
                - Format:
                  https://www.youtube.com/results?search_query=<dish_name>
                - Replace spaces with +

                8. STRICT RULES:
                - No markdown
                - No explanation
                - No extra keys
                - Valid JSON only

                9. SAFETY:
                - If not possible → return empty JSON
                - If no valid dish or recipe can be generated:
                  → return empty JSON or []
                - No exceptions
                - No invalid output
                - No extra text
                """.formatted(dishName.trim(), ingredientList, lang, noteLine);
    }


    // =========================
    // 🌐 LANGUAGE NORMALIZER
    // =========================
    private static String normalizeLanguage(String language) {
        if (language == null || language.isBlank()) return "English";

        String s = language.trim().toLowerCase(Locale.ROOT);

        return switch (s) {
            case "en", "english" -> "English";
            case "hi", "hindi" -> "Hindi";
            default -> "English";
        };
    }
}