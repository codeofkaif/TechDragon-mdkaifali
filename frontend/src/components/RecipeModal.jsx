import React from "react";

function RecipeModal({ recipe, loading, onClose }) {
  return (
    <div className="overlay">
      <div className="modal">
        <div className="panel-header">
          <h2>{loading ? "Preparing Recipe..." : recipe?.name}</h2>
          <button type="button" className="close-button" onClick={onClose}>
            ✖
          </button>
        </div>

        {loading ? (
          <p className="status-text">Generating recipe steps... 🍲</p>
        ) : (
          <div className="recipe-content">
            <h4>Step-by-step instructions</h4>
            <ul>
              {recipe?.steps?.map((step) => (
                <li key={step}>{step}</li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  );
}

export default RecipeModal;
