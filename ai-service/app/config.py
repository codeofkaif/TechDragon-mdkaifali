"""Environment-driven settings for the AI recipe service."""

from __future__ import annotations

from pydantic import Field
from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    hf_model_fine_tuned: str | None = Field(
        default=None,
        validation_alias="HF_MODEL_FINE_TUNED",
    )
    hf_model_base: str | None = Field(
        default=None,
        validation_alias="HF_MODEL_BASE",
    )
    hf_token: str | None = Field(default=None, validation_alias="HF_TOKEN")

    ollama_base_url: str = Field(
        default="http://localhost:11434",
        validation_alias="OLLAMA_BASE_URL",
    )
    ollama_model: str = Field(default="gemma:2b", validation_alias="OLLAMA_MODEL")
    ollama_temperature: float = Field(default=0.2, validation_alias="OLLAMA_TEMPERATURE")

    request_timeout_seconds: float = Field(
        default=120.0,
        validation_alias="REQUEST_TIMEOUT_SECONDS",
    )
    ollama_timeout_seconds: float = Field(
        default=120.0,
        validation_alias="OLLAMA_TIMEOUT_SECONDS",
    )
    max_new_tokens: int = Field(default=1024, validation_alias="HF_MAX_NEW_TOKENS")


def load_settings() -> Settings:
    return Settings()
