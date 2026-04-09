"""Orchestrates generation: HF (lazy) → Ollama, plus structured recipe helper for /generate."""

from __future__ import annotations

import json
import logging
import re
from typing import Any

from app.config import Settings
from app.model_loader import get_hf_client
from app.ollama_client import OllamaError, generate_text as ollama_generate

_log = logging.getLogger(__name__)

STRUCTURED_RECIPE_INSTRUCTIONS = """
Generate a recipe using the given ingredients with structured JSON output only.
Return exactly one JSON object with these keys:
- dish_name (string)
- ingredients (array of strings)
- steps (array of strings)
- tips (string)

Rules:
- No markdown, no code fences, no commentary before or after the JSON.
- Ingredients should include the listed items plus reasonable pantry items if needed.
""".strip()


def _strip_fences(text: str) -> str:
    s = (text or "").strip()
    s = re.sub(r"^```(?:json)?\s*", "", s, flags=re.IGNORECASE)
    s = re.sub(r"\s*```$", "", s)
    return s.strip()


def extract_first_json_object(text: str) -> dict[str, Any]:
    """Parse the first top-level JSON object from model output."""
    cleaned = _strip_fences(text)
    try:
        node = json.loads(cleaned)
        if isinstance(node, dict):
            return node
    except json.JSONDecodeError:
        pass
    start = cleaned.find("{")
    end = cleaned.rfind("}")
    if start == -1 or end == -1 or end <= start:
        raise ValueError("No JSON object found in model output")
    return json.loads(cleaned[start : end + 1])


def normalize_structured_recipe(data: dict[str, Any]) -> dict[str, Any]:
    """Ensure API contract keys exist with safe types."""
    name = data.get("dish_name") or data.get("dishName") or ""
    ing = data.get("ingredients")
    steps = data.get("steps")
    tips = data.get("tips") or ""

    def as_str_list(v: Any) -> list[str]:
        if v is None:
            return []
        if isinstance(v, list):
            return [str(x).strip() for x in v if str(x).strip()]
        if isinstance(v, str) and v.strip():
            return [v.strip()]
        return []

    return {
        "dish_name": str(name).strip(),
        "ingredients": as_str_list(ing),
        "steps": as_str_list(steps),
        "tips": str(tips).strip(),
    }


def generate_raw(prompt: str, settings: Settings) -> str:
    """
    Run the full prompt through the active backend (HF if loaded, else Ollama).
    Used by Spring Boot via POST /v1/prompt — output must stay model-raw for Java-side JSON parsing.
    """
    prompt = (prompt or "").strip()
    if not prompt:
        raise ValueError("prompt is empty")

    hf = get_hf_client(settings)
    if hf is not None:
        try:
            _log.debug("Generating via Hugging Face")
            return hf.generate(prompt)
        except Exception as e:
            _log.warning("HF generation failed, falling back to Ollama: %s", e)

    _log.debug("Generating via Ollama")
    try:
        return ollama_generate(prompt, settings)
    except OllamaError:
        raise
    except Exception as e:
        raise OllamaError(str(e)) from e


def generate_structured_from_ingredients(ingredients_csv: str, settings: Settings) -> dict[str, Any]:
    """Build the /generate?ingredients= response body."""
    raw_ing = (ingredients_csv or "").strip()
    if not raw_ing:
        raise ValueError("ingredients query parameter is required")

    prompt = f"{STRUCTURED_RECIPE_INSTRUCTIONS}\n\nIngredients: {raw_ing}\n"
    raw = generate_raw(prompt, settings)
    try:
        obj = extract_first_json_object(raw)
        return normalize_structured_recipe(obj)
    except Exception as e:
        _log.exception("Failed to parse structured recipe JSON")
        raise ValueError(f"Model did not return valid structured JSON: {e}") from e
