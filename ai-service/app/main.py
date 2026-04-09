"""
FastAPI entrypoint: GET /generate, POST /v1/prompt, GET /health.

Run from repository root:
  cd ai-service && uvicorn app.main:app --host 0.0.0.0 --port 8000
"""

from __future__ import annotations

import logging
import sys
from typing import Any

from fastapi import FastAPI, HTTPException, Query

from app.config import load_settings
from app.model_loader import configured_priority
from app.ollama_client import OllamaError
from app.schemas import PromptBody, PromptResponse, StructuredRecipeResponse
from app.service import generate_raw, generate_structured_from_ingredients

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
    stream=sys.stdout,
)
_log = logging.getLogger("ai-service")

settings = load_settings()
app = FastAPI(
    title="AiRasoi AI Service",
    version="1.0.0",
    description="Hugging Face (optional) → Ollama fallback; integrates with Spring Boot on :8080.",
)


@app.get("/health", tags=["ops"])
def health() -> dict[str, Any]:
    return {
        "status": "ok",
        "priority_backend": configured_priority(settings),
        "hf_fine_tuned_set": bool(settings.hf_model_fine_tuned and settings.hf_model_fine_tuned.strip()),
        "hf_base_set": bool(settings.hf_model_base and settings.hf_model_base.strip()),
    }


@app.get("/generate", response_model=StructuredRecipeResponse, tags=["recipes"])
def generate(
    ingredients: str = Query("", description="Comma-separated ingredients"),
) -> StructuredRecipeResponse:
    try:
        data = generate_structured_from_ingredients(ingredients, settings)
        return StructuredRecipeResponse(**data)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e)) from e
    except OllamaError as e:
        _log.warning("Ollama error on /generate: %s", e)
        raise HTTPException(status_code=503, detail=str(e)) from e
    except Exception:
        _log.exception("Unexpected error on /generate")
        raise HTTPException(status_code=502, detail="Generation failed") from None


@app.post("/v1/prompt", response_model=PromptResponse, tags=["spring"])
def prompt_generate(body: PromptBody) -> PromptResponse:
    """
    Spring Boot AiModelClient compatibility: same prompts as Ollama; returns raw model text in JSON.
    """
    try:
        text = generate_raw(body.prompt, settings)
        return PromptResponse(text=text)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e)) from e
    except OllamaError as e:
        _log.warning("Ollama error on /v1/prompt: %s", e)
        raise HTTPException(status_code=503, detail=str(e)) from e
    except Exception:
        _log.exception("Unexpected error on /v1/prompt")
        raise HTTPException(status_code=502, detail="Generation failed") from None


if __name__ == "__main__":
    import uvicorn

    uvicorn.run("app.main:app", host="0.0.0.0", port=8000, reload=False)
