# AiRasoi AI service (FastAPI)

Python layer on **port 8000**: optional Hugging Face local inference, then **Ollama** fallback. Spring Boot (**8080**) calls this service for structured recipes and prompt completion.

## Layout

```
ai-service/
├── app/
│   ├── __init__.py
│   ├── main.py          # FastAPI app + routes
│   ├── config.py        # env / Settings
│   ├── schemas.py       # request/response models
│   ├── service.py       # generation orchestration
│   ├── model_loader.py  # lazy HF model selection
│   ├── hf_client.py     # Transformers pipeline (optional)
│   └── ollama_client.py # httpx → Ollama /api/generate
├── requirements.txt
├── .env.example
└── README.md
```

## Run

```bash
cd ai-service
python -m venv .venv
source .venv/bin/activate   # Windows: .venv\Scripts\activate
pip install -r requirements.txt
cp .env.example .env        # optional
uvicorn app.main:app --host 0.0.0.0 --port 8000
```

- OpenAPI: http://localhost:8000/docs  
- Health: http://localhost:8000/health  

Ensure **Ollama** is running if you are not using a local HF model.

## Environment

See `.env.example`. Priority: **HF_MODEL_FINE_TUNED** → **HF_MODEL_BASE** → **Ollama**.

- **Train → Hub → this service → Spring:** `../docs/HF_TRAIN_AND_CONNECT.md`
- **Example API calls:** `../docs/API_EXAMPLES.md`
