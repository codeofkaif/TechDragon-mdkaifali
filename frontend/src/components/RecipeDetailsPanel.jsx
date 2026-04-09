import React from "react";

function RecipeDetailsPanel({ selectedDish }) {
  if (!selectedDish) {
    return (
      <aside className="recipe-details-pane empty">
        <div className="recipe-empty-content">
          <span>🍽️</span>
          <p>Your recipe will appear here 🍽️</p>
          <small>Select a dish from the left to view details</small>
        </div>
      </aside>
    );
  }

  return (
    <aside className="recipe-details-pane">
      <div className="recipe-hero">
        <div className="recipe-hero-overlay" />
      </div>

      <h2>{selectedDish.name}</h2>

      <div className="badge-row">
        <span className="badge badge-region">{selectedDish.region}</span>
        <span className="badge badge-time">{selectedDish.time} min</span>
        <span className="badge badge-taste">{selectedDish.taste}</span>
      </div>

      <div className="recipe-info-row">
        <span>⏱ {selectedDish.time} min</span>
        <span>🧑‍🍳 Easy</span>
        <span>🍽 2 servings</span>
      </div>

      <p className="recipe-description">{selectedDish.description}</p>

      <div className="recipe-action-row">
        <button type="button" className="recipe-action-btn save">
          Save Recipe
        </button>
        <button type="button" className="recipe-action-btn share">
          Share
        </button>
      </div>

      <section className="recipe-subsection separated">
        <h3>Ingredients</h3>
        <ul className="ingredients-list">
          {selectedDish.ingredients?.map((ingredient) => (
            <li key={ingredient}>{ingredient}</li>
          ))}
        </ul>
      </section>

      <section className="recipe-subsection separated">
        <h3>Steps</h3>
        <div className="steps-list">
          {selectedDish.steps?.map((step, index) => (
            <div className="step-row" key={step}>
              <span className="step-number">{index + 1}</span>
              <p>{step}</p>
            </div>
          ))}
        </div>
      </section>
    </aside>
  );
}

export default RecipeDetailsPanel;
