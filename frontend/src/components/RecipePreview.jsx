import React from "react";

const sampleDishes = [
  {
    name: "Aloo Jeera",
    region: "North Indian",
    time: "15 min",
    taste: "Spicy",
    description: "Simple potato dish with cumin and spices.",
  },
  {
    name: "Masala Potato Fry",
    region: "South Indian",
    time: "20 min",
    taste: "Medium",
    description: "Crispy potato fry with aromatic masala.",
  },
  {
    name: "Paneer Bhurji",
    region: "North Indian",
    time: "25 min",
    taste: "Mild",
    description: "Soft paneer scramble with rich onion-tomato mix.",
  },
];

function RecipePreview() {
  return (
    <section className="preview-section">
      <h3>Recipe Suggestions</h3>
      <div className="preview-grid">
        {sampleDishes.map((dish) => (
          <article key={dish.name} className="preview-card">
            <div className="preview-image" />
            <h4>{dish.name}</h4>
            <div className="badge-row">
              <span className="badge badge-region">{dish.region}</span>
              <span className="badge badge-time">{dish.time}</span>
              <span className="badge badge-taste">{dish.taste}</span>
            </div>
            <p>{dish.description}</p>
            <button type="button" className="view-recipe-button">
              View Recipe
            </button>
          </article>
        ))}
      </div>
    </section>
  );
}

export default RecipePreview;
