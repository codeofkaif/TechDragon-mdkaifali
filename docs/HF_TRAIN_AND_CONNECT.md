# Train on Hugging Face → FastAPI → Spring Boot (without breaking existing code)

This stack is already wired: **Python** tries your Hugging Face model first, then **Ollama**. **Spring** talks only to **Python** (`:8000`) and **Ollama** (fallback); it never loads HF weights. Existing APIs and defaults stay the same if you do nothing new.

---

## 1. Train tomorrow

- Fine-tune in Colab / local GPU as you prefer (e.g. LoRA on a base such as Gemma).
- For **this** service, the Hub repo you set in `HF_MODEL_FINE_TUNED` must be loadable with:

  `transformers` `pipeline("text-generation", model=<repo_id>, ...)`

- **Practical rule:** push a **merged** full model (or a standalone causal LM checkpoint), not raw adapter-only weights only, unless you extend `app/hf_client.py` to load base + adapters yourself.

---

## 2. Upload to Hugging Face

1. Create a model repo: `https://huggingface.co/<your_org>/<your-model-name>`.
2. Push weights + tokenizer (`upload_folder`, `push_to_hub`, or Git LFS).
3. If the repo is **private** or **gated**, create a **read** token at [HF settings](https://huggingface.co/settings/tokens).

---

## 3. Load in Python FastAPI

From `ai-service/`:

```bash
source .venv/bin/activate
pip install -r requirements.txt
pip install torch   # or CUDA build from https://pytorch.org
pip install -r requirements-hf.txt
```

Copy `.env.example` → `.env` and set:

| Variable | Purpose |
|----------|---------|
| `HF_MODEL_FINE_TUNED` | Repo id, e.g. `your-org/your-recipe-model` |
| `HF_TOKEN` | Required for private/gated models |
| `HF_MODEL_BASE` | Optional fallback if fine-tuned load fails (e.g. same base you trained on) |

Leave **Ollama** env vars set: if HF fails to load or errors at runtime, the app falls back to Ollama automatically.

Start the API:

```bash
cd ai-service && uvicorn app.main:app --host 0.0.0.0 --port 8000
```

**First generation** can be slow (model download + load). Check logs for `Using Hugging Face backend` or `No local HF model loaded` (Ollama path).

`GET http://localhost:8000/health` shows whether `HF_MODEL_FINE_TUNED` / `HF_MODEL_BASE` are set (not whether weights finished loading).

---

## 4. Connect Spring Boot

No code changes required.

1. Run Spring with **`ai.provider: ollama`** (default) so the **Ollama** bean exists for fallback.
2. Point Java at Python:

   - `AI_SERVICE_URL=http://localhost:8000`  
   - or `ai.fastapi.base-url` in `application.yml`

3. Start Spring on **8080** as usual.

Flow:

- **Dishes / full recipe prompts** → `PythonFirstAiModelClient` → `POST http://localhost:8000/v1/prompt` → (HF in Python, else Ollama in Python) → if Python is down, **Java** uses **Ollama** directly.
- **`GET /recipe?ingredients=`** → `GET http://localhost:8000/generate?...` → same HF→Ollama chain inside Python → on failure, Java uses **Ollama** recipe path.

---

## 5. Safety checklist (existing code keeps working)

| Concern | Mitigation |
|--------|------------|
| HF not installed / wrong env | Service uses Ollama; Spring still works. |
| Python not running | Spring falls back to Ollama. |
| Bad HF repo id | Load fails at startup of first request; logs warning; Ollama used. |
| Do not break Java API | Do not remove `POST /generate-recipe`, `GET /recipe`, or change response DTOs. |
| Do not break defaults | Keep `ai.provider: ollama`; optional HF is entirely in Python + env. |

---

## Quick verification

```bash
# Python + HF (after .env is set)
curl -s http://localhost:8000/health | jq

# Spring → Python /generate
curl -sG "http://localhost:8080/recipe" --data-urlencode "ingredients=onion,tomato" | jq
```

See also: `docs/API_EXAMPLES.md`, `docs/SPRING_AI_INTEGRATION.md`, `ai-service/README.md`.
