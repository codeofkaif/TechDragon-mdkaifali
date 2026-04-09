"""Ollama HTTP client with timeouts and structured error handling."""

from __future__ import annotations

import logging
from typing import Any

import httpx

from app.config import Settings

_log = logging.getLogger(__name__)


class OllamaError(RuntimeError):
    """Raised when Ollama returns an error payload or invalid response."""


def generate_text(prompt: str, settings: Settings) -> str:
    """Call Ollama /api/generate (non-streaming) and return model text."""
    base = settings.ollama_base_url.rstrip("/")
    url = f"{base}/api/generate"
    timeout = httpx.Timeout(settings.ollama_timeout_seconds)
    payload: dict[str, Any] = {
        "model": settings.ollama_model,
        "prompt": prompt,
        "stream": False,
        "options": {"temperature": settings.ollama_temperature},
    }
    _log.debug("Ollama request model=%s", settings.ollama_model)
    try:
        with httpx.Client(timeout=timeout) as client:
            resp = client.post(url, json=payload)
            resp.raise_for_status()
            data = resp.json()
    except httpx.TimeoutException as e:
        raise OllamaError(f"Ollama request timed out after {settings.ollama_timeout_seconds}s") from e
    except httpx.HTTPStatusError as e:
        raise OllamaError(f"Ollama HTTP {e.response.status_code}: {e.response.text[:500]}") from e
    except httpx.RequestError as e:
        raise OllamaError(f"Ollama connection failed: {e}") from e

    err = data.get("error")
    if err:
        raise OllamaError(str(err))

    response_text = data.get("response")
    if response_text is None:
        raise OllamaError("Missing 'response' in Ollama payload")

    return str(response_text)
