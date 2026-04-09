<<<<<<< HEAD
# 🧑🏻‍💻 Kalpathon Hackathon Submission  

---

## 🧠 Team Name  
TachDragon  

---

## 🚀 Project Name  
AI Rasoi – Personalized Recipe Generator  

---

## 🤖 Selected Track  
AI / SLM Fine-Tuning  

---

## 💡 Selected Problem Statement (PS)  
Problem 2: Personalized Recipe Generator  

---

## 👤 Team Leader  
Name: Md Kaif Ali  
Phone: 6388913772  

---

## 👥 Team Members & Roles  

| Name              | Role                  |
|------------------|----------------------|
| Md Kaif Ali      | AI/ML,Backend        |
| Mojammil Husain  | AI/ML,Backend        |
| Jishan           | Frontend Developer   |

---

## 📌 Project Description  

### 🔍 Problem  
Many people do not know what to cook with the ingredients they already have. Searching recipes takes time and is often confusing.  

---

### 💡 Solution  
We are building an AI-powered system that takes user-input ingredients and generates a complete recipe with step-by-step instructions, focused on Indian cuisine.  

---

### ⚙️ Key Features  
- Ingredient-based recipe generation  
- Step-by-step cooking instructions  
- Indian cuisine focus  
- Simple and user-friendly interface  

---

## 🛠️ Tech Stack  
- Frontend: React.js (Web Interface)  
- Backend: Spring Boot (Java)  
- AI/ML: Gemma 3 270M   
- Database: PostgreSQL (with JPA/Hibernate)
---

### 🌍 Impact  
This project helps users save time, reduce food waste, and cook easily using available ingredients. It is especially useful for students and beginners.  


## 🚀 Features

### 🧾 Ingredient Input (Chips UI)

* Add multiple ingredients using input field
* Press Enter to convert into chips
* Remove ingredients easily using ❌

---

### 🍲 Dish Suggestions (AI Simulated)

* Click **"Show Dishes"** to generate recipe suggestions
* Uses mock data to simulate AI response
* Displays multiple dishes in card format

---

### 🏷️ Smart Dish Cards

Each dish includes:

* Dish name
* Region (e.g., North Indian)
* Cooking time
* Taste level (Spicy, Medium, Mild)
* Short description
* "View Recipe 🍳" button

---

### 🎯 Filter System (LeetCode Inspired)

* Filter dishes by:

  * Region
  * Cooking Time
  * Taste
* Clean popup filter panel
* Apply / Reset filters dynamically

---

### 🤖 View Recipe (Simulated AI)

* Click "View Recipe" to see full recipe
* Opens in modal
* Step-by-step cooking instructions

---

### ⏳ Loading & Empty States

* Loading message while fetching dishes:

  > "Finding best dishes for you... 🍳"
* Empty state if no results found

---

## 🛠️ Tech Stack

* React (Functional Components + Hooks)
* JavaScript (ES6+)
* CSS / Tailwind (for styling)
* No backend (mock data used)

---

## 📂 Project Structure

```
src/
│
├── App.js
├── components/
│   ├── IngredientInput.js
│   ├── DishList.js
│   ├── DishCard.js
│   ├── FilterPanel.js
│   └── RecipeModal.js
```

---

## ⚙️ How It Works

1. User enters ingredients
2. Clicks **"Show Dishes"**
3. Mock AI generates dish suggestions
4. User applies filters (optional)
5. Selects a dish
6. Views full recipe in modal

---

## 🎨 UI Highlights

* Clean and minimal design
* Responsive layout (mobile + desktop)
* Card-based interface
* Smooth user experience

---

## ⚠️ Note

* This project currently uses **mock data** to simulate AI responses
* Backend and real AI integration will be added later

---

## 🚀 Future Improvements

* Integrate real AI model (SLM fine-tuned)
* Add nutrition information
* Voice input support
* Regional language support (Hindi / Hinglish)

---

 

=======
## AiRasoi

Full-stack recipe assistant:
- **Backend**: Spring Boot (Java 21) + PostgreSQL
- **Frontend**: React + Vite + Tailwind
- **AI (default)**: Ollama (local)
- **AI (optional)**: Python FastAPI layer (`ai-service/`) for Hugging Face / structured generation

### Requirements
- Java 21
- PostgreSQL
- Ollama (for local AI)

### Configure PostgreSQL
The app reads connection info from env vars (defaults shown):

- `DB_URL` (default `jdbc:postgresql://localhost:5432/airasoi`)
- `DB_USERNAME` (default is your shell user, `${USER}`)
- `DB_PASSWORD` (default empty)
- `JPA_DDL_AUTO` (default `update`)

Create the database:

```sql
create database airasoi;
```

### Run

```bash
./mvnw spring-boot:run
```

### Run with Ollama (recommended)
1. Start Ollama:

```bash
ollama serve
```

2. Pull the configured model (default is `gemma:2b`):

```bash
ollama pull gemma:2b
```

3. Start the backend:

```bash
./mvnw spring-boot:run
```

## Frontend (React + Tailwind)

From `frontend/`:

```bash
npm install
npm run dev
```

The Vite dev server proxies API requests:
- Frontend calls `"/api/*"`
- Proxy forwards to backend `http://localhost:8080/*`

## API quick test

```bash
curl "http://localhost:8080/recipe?ingredients=tomato,onion,garlic"
```

### Frontend architecture

- `frontend/src/App.tsx`: app state + wiring (no heavy UI markup)
- `frontend/src/panels/*`: layout panels
  - `ChatPanel`: chat UI + ingredient extraction/tags + edit tags
  - `DishListPanel`: filters + dish list
  - `RecipePanel`: selected dish + tabs + steps + YouTube button
- `frontend/src/lib/*`: reusable pure helpers (filters/sort, rating, ingredient extraction, images)

### API

#### POST `/generate-dishes`
Request:

```json
{
  "ingredients": ["chicken", "rice", "tomato"],
  "cuisine": "Indian",
  "count": 5
}
```

#### POST `/generate-dishes/ai`
Generates dish ideas via AI and returns a JSON list with tags: `name`, `region`, `type`, `time`, `description`.

Request:

```json
{
  "ingredients": ["chicken", "rice", "tomato"]
}
```

Response:

```json
{
  "generatedAt": "2026-04-08T00:00:00Z",
  "dishes": [
    {
      "name": "Tomato Rice Skillet",
      "region": "South India",
      "type": "dinner",
      "time": "25 min",
      "description": "A one-pan tomato-spiced rice dish with tender chicken and bright acidity."
    }
  ]
}
```

#### POST `/generate-recipe`
Generates a recipe via AI.

Request:

```json
{
  "dishName": "Chicken Biryani",
  "ingredients": ["chicken", "rice", "onion", "tomato", "yogurt"]
}
```

Response:

```json
{
  "recipe": "A fragrant one-pot rice dish...",
  "steps": ["...", "..."],
  "time": "60 min",
  "youtubeLink": "https://www.youtube.com/results?search_query=Chicken%20Biryani%20recipe"
}
```

### AI configuration

**Spring Boot:** `PythonFirstAiModelClient` calls the **Python** service first (`POST /v1/prompt` on `AI_SERVICE_URL`), then falls back to **Ollama** if Python is down or errors. Keep `ai.provider: ollama` so the Ollama bean exists for fallback.

- **Set** `OLLAMA_BASE_URL` (default `http://localhost:11434`)
- **Set** `OLLAMA_MODEL` (default `gemma:2b`)
- **Set** `OLLAMA_TEMPERATURE` (default `0.2`)
- **Set** `AI_SERVICE_URL` / `ai.fastapi.base-url` (default `http://localhost:8000`) for the Python layer

Start Ollama:

```bash
ollama serve
ollama pull gemma:2b
```

**Python ai-service** (Hugging Face + Ollama chain inside Python):

1. Run the service from `ai-service/` (see `requirements.txt` and env vars below).
2. Spring uses the same prompts as a direct Ollama call; recipe/dish JSON parsing stays in Java.

Python env (see `ai-service/.env.example` and `ai-service/app/config.py`): `HF_MODEL_FINE_TUNED`, `HF_MODEL_BASE`, `HF_TOKEN`, `OLLAMA_*`, etc. After training, install HF deps: `pip install -r ai-service/requirements-hf.txt` (plus `torch`). End-to-end checklist: **`docs/HF_TRAIN_AND_CONNECT.md`**. Run: `cd ai-service && uvicorn app.main:app --host 0.0.0.0 --port 8000`. Examples: `docs/API_EXAMPLES.md`.

### Fine-tuning workflow (Hugging Face + Colab + LoRA)

Spring prefers **Python** when reachable, then **Ollama**. The Python service itself can use HF models with an Ollama fallback. For custom behavior, use this training flow:

1. Choose a Hugging Face Gemma base model.
2. Fine-tune in Google Colab using LoRA adapters.
3. Save and publish adapters to Hugging Face.
4. Merge/export to a GGUF model for Ollama serving.
5. Load the model in Ollama and set `OLLAMA_MODEL` to that model tag.

## GitHub notes
- This repo ignores local artifacts like `target/`, `ai-service/.venv/`, and `dataset/`.
- Do not commit secrets. Use environment variables / `.env` files locally.
>>>>>>> ffec9fd (last submission)
