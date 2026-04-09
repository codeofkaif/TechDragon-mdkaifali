import React from "react";

function DishCard({ dish, onViewRecipe }) {
  return (
    <article className="dish-card">
      <div className="dish-card-image" />
      <h3>{dish.name}</h3>
      <div className="badge-row">
        <span className="badge badge-region">{dish.region}</span>
        <span className="badge badge-time">{dish.time} min</span>
        <span className="badge badge-taste">{dish.taste}</span>
      </div>
      <p>{dish.description}</p>
      <button
        type="button"
        className="view-recipe-button"
        onClick={() => onViewRecipe(dish)}
      >
        View Recipe 🍳
      </button>
    </article>
  );
}

export default DishCard;
