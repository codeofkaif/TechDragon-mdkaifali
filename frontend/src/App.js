import React, { useMemo, useState } from "react";
import IngredientInput from "./components/IngredientInput";
import DishList from "./components/DishList";
import FilterPanel from "./components/FilterPanel";
import RecipeModal from "./components/RecipeModal";
import "./styles.css";

const MOCK_DISHES = [
  {
    id: 1,
    name: "Aloo Jeera",
    region: "North Indian",
    time: 15,
    taste: "Spicy",
    description: "Simple potato dish with cumin",
  },
  {
    id: 2,
    name: "Masala Potato Fry",
    region: "South Indian",
    time: 20,
    taste: "Medium",
    description: "Crispy spiced potato fry",
  },
  {
    id: 3,
    name: "Lemon Rice",
    region: "South Indian",
    time: 18,
    taste: "Tangy",
    description: "Fragrant rice tempered with lemon and spices",
  },
  {
    id: 4,
    name: "Paneer Bhurji",
    region: "North Indian",
    time: 25,
    taste: "Mild",
    description: "Scrambled cottage cheese with masala",
  },
];

const initialFilters = {
  region: "",
  time: "",
  taste: "",
};

function App() {
  const [ingredients, setIngredients] = useState([]);
  const [dishes, setDishes] = useState([]);
  const [filters, setFilters] = useState(initialFilters);
  const [loading, setLoading] = useState(false);
  const [selectedRecipe, setSelectedRecipe] = useState(null);
  const [isRecipeModalOpen, setIsRecipeModalOpen] = useState(false);
  const [isFilterPanelOpen, setIsFilterPanelOpen] = useState(false);
  const [recipeLoading, setRecipeLoading] = useState(false);

  const handleShowDishes = () => {
    setLoading(true);
    setTimeout(() => {
      // Simulated API call that can use ingredients for personalization.
      setDishes(MOCK_DISHES);
      setLoading(false);
    }, 1400);
  };

  const filteredDishes = useMemo(() => {
    return dishes.filter((dish) => {
      const matchesRegion = !filters.region || dish.region === filters.region;
      const matchesTime = !filters.time || dish.time <= Number(filters.time);
      const matchesTaste = !filters.taste || dish.taste === filters.taste;

      return matchesRegion && matchesTime && matchesTaste;
    });
  }, [dishes, filters]);

  const handleApplyFilters = (nextFilters) => {
    setFilters(nextFilters);
    setIsFilterPanelOpen(false);
  };

  const handleResetFilters = () => {
    setFilters(initialFilters);
  };

  const handleViewRecipe = (dish) => {
    setRecipeLoading(true);
    setIsRecipeModalOpen(true);

    setTimeout(() => {
      setSelectedRecipe({
        ...dish,
        steps: [
          "Step 1: Heat oil in a pan.",
          "Step 2: Add cumin, onions, and listed ingredients.",
          "Step 3: Cook for 10 minutes and garnish before serving.",
        ],
      });
      setRecipeLoading(false);
    }, 1200);
  };

  const closeRecipeModal = () => {
    setIsRecipeModalOpen(false);
    setSelectedRecipe(null);
    setRecipeLoading(false);
  };

  return (
    <div className="app-shell">
      <div className="container">
        <header className="top-header">
          <h1>🍳 AI Recipe Generator</h1>
        </header>

        <section className="input-and-filter">
          <IngredientInput
            ingredients={ingredients}
            setIngredients={setIngredients}
            onShowDishes={handleShowDishes}
          />
          <button
            className="filter-button"
            type="button"
            onClick={() => setIsFilterPanelOpen(true)}
          >
            ⚙️ Filter
          </button>
        </section>

        {loading ? (
          <p className="status-text">Finding best dishes for you... 🍳</p>
        ) : (
          <DishList dishes={filteredDishes} onViewRecipe={handleViewRecipe} />
        )}
      </div>

      {isFilterPanelOpen && (
        <FilterPanel
          filters={filters}
          onApply={handleApplyFilters}
          onReset={handleResetFilters}
          onClose={() => setIsFilterPanelOpen(false)}
        />
      )}

      {isRecipeModalOpen && (
        <RecipeModal
          recipe={selectedRecipe}
          loading={recipeLoading}
          onClose={closeRecipeModal}
        />
      )}
    </div>
  );
}

export default App;
