import React, { useState } from "react";

function IngredientInput({ ingredients, setIngredients, onShowDishes }) {
  const [inputValue, setInputValue] = useState("");

  const addIngredient = () => {
    const trimmed = inputValue.trim();
    if (!trimmed) return;

    const exists = ingredients.some(
      (ingredient) => ingredient.toLowerCase() === trimmed.toLowerCase()
    );
    if (!exists) {
      setIngredients([...ingredients, trimmed]);
    }
    setInputValue("");
  };

  const handleKeyDown = (event) => {
    if (event.key === "Enter") {
      event.preventDefault();
      addIngredient();
    }
  };

  const removeIngredient = (ingredientToRemove) => {
    setIngredients(
      ingredients.filter((ingredient) => ingredient !== ingredientToRemove)
    );
  };

  return (
    <div className="ingredient-section">
      <label htmlFor="ingredient-input" className="input-label">
        Add Ingredients
      </label>
      <input
        id="ingredient-input"
        className="ingredient-input"
        type="text"
        value={inputValue}
        onChange={(event) => setInputValue(event.target.value)}
        onKeyDown={handleKeyDown}
        placeholder="Type ingredient and press Enter..."
      />

      <div className="chips-container">
        {ingredients.map((ingredient) => (
          <span className="chip" key={ingredient}>
            {ingredient}
            <button
              type="button"
              className="chip-remove"
              onClick={() => removeIngredient(ingredient)}
              aria-label={`Remove ${ingredient}`}
            >
              ❌
            </button>
          </span>
        ))}
      </div>

      <button
        type="button"
        className="show-dishes-button"
        onClick={onShowDishes}
        disabled={ingredients.length === 0}
      >
        Show Dishes
      </button>
    </div>
  );
}

export default IngredientInput;
