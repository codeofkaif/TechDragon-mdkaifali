# API examples

Assumptions: **FastAPI** `http://localhost:8000`, **Spring Boot** `http://localhost:8080`, **Ollama** on `11434` when used as fallback.

---

## FastAPI (Python)

### Health

```bash
curl -s http://localhost:8000/health | jq
```

### Structured recipe by ingredients

`GET /generate?ingredients=` — comma-separated list.

```bash
curl -sG "http://localhost:8000/generate" \
  --data-urlencode "ingredients=onion, tomato, potato, cumin" | jq
```

Example JSON shape:

```json
{
  "dish_name": "…",
  "ingredients": ["…"],
  "steps": ["…"],
  "tips": "…"
}
```

### Raw model text (Spring `AiModelClient` contract)

`POST /v1/prompt` — body is the same full prompt string Java sends to Ollama.

```bash
curl -s http://localhost:8000/v1/prompt \
  -H "Content-Type: application/json" \
  -d '{"prompt":"Return ONLY valid JSON: {\"hello\":\"world\"}"}' | jq
```

Response:

```json
{ "text": "… model output …" }
```

---

## Spring Boot (Java)

### Recipe from ingredients (calls Python `/generate`, then Ollama on failure)

```bash
curl -sG "http://localhost:8080/recipe" \
  --data-urlencode "ingredients=onion, tomato, paneer" | jq
```

Response: `GenerateRecipeResponse` (`recipe`, `steps`, `time`, `youtubeLink`).

### Full recipe with dish name (unchanged contract)

```bash
curl -s http://localhost:8080/generate-recipe \
  -H "Content-Type: application/json" \
  -d '{
    "dishName": "Palak Paneer",
    "ingredients": ["spinach","paneer","onion","garlic"],
    "language": "English",
    "note": null
  }' | jq
```

### AI dish ideas (uses `PythonFirstAiModelClient` → Python `/v1/prompt` then Ollama)

```bash
curl -s http://localhost:8080/generate-dishes/ai \
  -H "Content-Type: application/json" \
  -d '{"ingredients":["potato","peas","cumin"]}' | jq
```


---

## Vite dev server (frontend proxy)

If the UI proxies `/api` → `8080` with path rewrite:

```bash
curl -sG "http://localhost:5173/api/recipe" \
  --data-urlencode "ingredients=rice, lentil" | jq
```
