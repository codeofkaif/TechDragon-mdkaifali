import React from "react";
import DishCard from "./DishCard.jsx";

const SUGGESTED_INGREDIENTS = ["onion", "potato", "tomato"];

function DishList({ dishes, onViewRecipe, onQuickAddIngredient }) {
  if (dishes.length === 0) {
    return (
      <div className="empty-state friendly-empty-state">
        <span className="empty-state-icon">🍳</span>
        <h3>Ready to cook something amazing?</h3>
        <div className="empty-suggestion-chips">
          {SUGGESTED_INGREDIENTS.map((ingredient) => (
            <button
              key={ingredient}
              type="button"
              className="empty-suggestion-chip"
              onClick={() => onQuickAddIngredient(ingredient)}
            >
              {ingredient}
            </button>
          ))}
        </div>
        <p>Add ingredients and click 'Show Dishes'</p>
      </div>
    );
  }

  return (
    <section className="dish-grid">
      {dishes.map((dish) => (
        <DishCard key={dish.id} dish={dish} onViewRecipe={onViewRecipe} />
      ))}
    </section>
  );
}

export default DishList;
