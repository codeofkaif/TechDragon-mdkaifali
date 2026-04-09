"""Pydantic models exposed by the HTTP API."""

from __future__ import annotations

from pydantic import BaseModel, Field


class PromptBody(BaseModel):
    """Body for POST /v1/prompt (Spring Boot AiModelClient compatibility)."""

    prompt: str = Field(
        ...,
        min_length=1,
        description="Full prompt; returned text is passed through for JSON parsing upstream",
    )


class PromptResponse(BaseModel):
    text: str


class StructuredRecipeResponse(BaseModel):
    """Response for GET /generate?ingredients= (Spring maps this to GenerateRecipeResponse)."""

    dish_name: str
    ingredients: list[str]
    steps: list[str]
    tips: str
