import React from "react";
import DishCard from "./DishCard";

function DishList({ dishes, onViewRecipe }) {
  if (dishes.length === 0) {
    return (
      <div className="empty-state">
        No dishes found, try different ingredients
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
