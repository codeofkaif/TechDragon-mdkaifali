"""Priority-based lazy selection: fine-tuned HF → base HF → (none → Ollama in service)."""

from __future__ import annotations

import logging
import threading
from typing import TYPE_CHECKING, Literal

from app.config import Settings

if TYPE_CHECKING:
    from app.hf_client import HfTextGenerator

_log = logging.getLogger(__name__)

BackendKind = Literal["hf_fine_tuned", "hf_base", "ollama"]

_lock = threading.Lock()
_hf_client: HfTextGenerator | None = None
_active_backend: BackendKind = "ollama"
_load_attempted = False


def reset_for_tests() -> None:
    global _hf_client, _active_backend, _load_attempted
    with _lock:
        _hf_client = None
        _active_backend = "ollama"
        _load_attempted = False


def try_load_hf(settings: Settings) -> tuple[HfTextGenerator | None, BackendKind]:
    """
    Try to construct an HF generator once per process, in priority order.
    Returns (client, kind) or (None, 'ollama') if both fail or are unset.
    """
    global _hf_client, _active_backend, _load_attempted

    with _lock:
        if _load_attempted:
            return _hf_client, _active_backend
        _load_attempted = True

        from app.hf_client import HfTextGenerator

        candidates: list[tuple[BackendKind, str | None]] = [
            ("hf_fine_tuned", settings.hf_model_fine_tuned),
            ("hf_base", settings.hf_model_base),
        ]
        for kind, model_id in candidates:
            if not model_id or not str(model_id).strip():
                continue
            mid = str(model_id).strip()
            try:
                client = HfTextGenerator(mid, settings.hf_token, settings.max_new_tokens)
                client.warmup()
                _hf_client = client
                _active_backend = kind
                _log.info("Using Hugging Face backend: %s (%s)", kind, mid)
                return _hf_client, _active_backend
            except Exception as e:
                _log.warning("HF model not usable (%s / %s): %s", kind, mid, e)

        _hf_client = None
        _active_backend = "ollama"
        _log.info("No local HF model loaded; generation will use Ollama fallback")
        return None, "ollama"


def get_hf_client(settings: Settings) -> HfTextGenerator | None:
    client, _ = try_load_hf(settings)
    return client


def active_backend_label(settings: Settings) -> BackendKind:
    _, kind = try_load_hf(settings)
    return kind


def configured_priority(settings: Settings) -> BackendKind:
    """Which backend will be tried first (does not load weights)."""
    if settings.hf_model_fine_tuned and str(settings.hf_model_fine_tuned).strip():
        return "hf_fine_tuned"
    if settings.hf_model_base and str(settings.hf_model_base).strip():
        return "hf_base"
    return "ollama"
