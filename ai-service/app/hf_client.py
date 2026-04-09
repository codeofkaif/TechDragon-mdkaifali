"""Lazy Hugging Face Transformers text generation (optional dependency)."""

from __future__ import annotations

import logging
import threading
from typing import Any

_log = logging.getLogger(__name__)


class HfTextGenerator:
    """Thread-safe lazy pipeline for a single model id."""

    def __init__(self, model_id: str, hf_token: str | None, max_new_tokens: int) -> None:
        self._model_id = model_id.strip()
        self._token = hf_token
        self._max_new_tokens = max_new_tokens
        self._lock = threading.Lock()
        self._pipe: Any = None

    def _ensure_pipeline(self) -> None:
        if self._pipe is not None:
            return
        with self._lock:
            if self._pipe is not None:
                return
            try:
                from transformers import pipeline
            except ImportError as e:
                raise RuntimeError(
                    "transformers is not installed. Install torch + transformers for local HF models."
                ) from e

            _log.info("Loading Hugging Face model %s (this may take a while)...", self._model_id)
            try:
                self._pipe = pipeline(
                    "text-generation",
                    model=self._model_id,
                    tokenizer=self._model_id,
                    token=self._token,
                    trust_remote_code=False,
                    device_map="auto",
                )
            except Exception as e:
                _log.warning("HF pipeline with device_map=auto failed (%s); retrying default device", e)
                self._pipe = pipeline(
                    "text-generation",
                    model=self._model_id,
                    tokenizer=self._model_id,
                    token=self._token,
                    trust_remote_code=False,
                )
            tok = self._pipe.tokenizer
            if getattr(tok, "pad_token_id", None) is None and getattr(tok, "eos_token_id", None) is not None:
                tok.pad_token = tok.eos_token
            _log.info("Hugging Face model ready: %s", self._model_id)

    def warmup(self) -> None:
        """Load weights into memory (call once at startup or first request)."""
        self._ensure_pipeline()

    def generate(self, prompt: str) -> str:
        self._ensure_pipeline()
        assert self._pipe is not None
        eos_id = getattr(self._pipe.tokenizer, "eos_token_id", None)
        out = self._pipe(
            prompt,
            max_new_tokens=self._max_new_tokens,
            do_sample=True,
            temperature=0.2,
            pad_token_id=eos_id,
        )
        if not out or not isinstance(out, list):
            raise RuntimeError("Unexpected HF pipeline output")
        text = out[0].get("generated_text", "")
        if isinstance(text, str) and text.startswith(prompt):
            text = text[len(prompt) :].lstrip()
        return text if isinstance(text, str) else str(text)
