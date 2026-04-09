# Spring Boot ↔ Python AI integration

## Configuration (`application.yml`)

Under `ai`:

| Key | Role |
|-----|------|
| `provider` | `ollama` — registers `OllamaAiModelClient` for fallback wiring. |
| `ollama.*` | Direct Ollama HTTP (`/api/generate`). |
| `fastapi.*` | Python service base URL, timeouts, retries, optional cache for `GET /generate`. |

Env overrides: `AI_SERVICE_URL`, `AI_SERVICE_RETRY_MAX_ATTEMPTS`, `AI_SERVICE_CACHE_ENABLED`, etc.

## Java classes (`com.airasoi.ai`)

| Class | Role |
|-------|------|
| `AiModelClient` | Contract: `generate(String prompt)` → raw model text. |
| `PythonAiClient` | HTTP to Python: `GET {base}{generate-path}?ingredients=`, `POST /v1/prompt`; retries, validation, optional Caffeine cache. |
| `PythonFirstAiModelClient` | `@Primary` implementation: try Python `/v1/prompt`, then `OllamaAiModelClient` if available. |
| `OllamaAiModelClient` | Unchanged Ollama integration; `@ConditionalOnProperty` `ai.provider=ollama` (default). |
| `FastApiAiProperties` | `ai.fastapi` binding (URL, paths, retry, cache). |
| `PythonResponseValidators` | JSON/text checks for Python responses. |

## Java services (`com.airasoi.service`)

| Class | Role |
|-------|------|
| `AiRecipeGeneratorService` | Builds prompts, calls `AiModelClient`, parses recipe JSON; exposes `generateRecipeWithClient` for Ollama-only fallback. |
| `AiDishGeneratorService` | Dish list via `AiModelClient`. |
| `RecipeFromIngredientsQueryService` | `GET /recipe`: Python `/generate` first, then Ollama recipe pipeline on failure. |

## Controllers

| Endpoint | Flow |
|----------|------|
| `GET /recipe?ingredients=` | `RecipeFromIngredientsQueryService` → `PythonAiClient.fetchStructuredRecipeByIngredients` → map to `GenerateRecipeResponse` → Ollama fallback. |
| `POST /generate-recipe` | `AiRecipeGeneratorService` → `AiModelClient` (Python first, then Ollama). |

## Python service layout

See `ai-service/README.md` — package `app` with `app.main:app` ASGI entrypoint.
