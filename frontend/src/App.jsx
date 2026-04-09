import React, { useMemo, useState } from "react";
import LandingPage from "./components/LandingPage.jsx";
import Sidebar from "./components/Sidebar.js";
import IngredientInput from "./components/IngredientInput.jsx";
import DishList from "./components/DishList.jsx";
import FilterChips from "./components/FilterChips.jsx";
import RecipeDetailsPanel from "./components/RecipeDetailsPanel.jsx";
import "./styles.css";

const initialFilters = {
  region: [],
  time: [],
  taste: [],
};

function toMinutes(value) {
  if (typeof value !== "string") return 30;
  const text = value.toLowerCase();
  const numberMatch = text.match(/\d+/);
  if (numberMatch) return Number(numberMatch[0]);
  if (text.includes("quick")) return 15;
  if (text.includes("medium")) return 30;
  if (text.includes("long")) return 45;
  return 30;
}

function App() {
  const [ingredients, setIngredients] = useState([]);
  const [dishes, setDishes] = useState([]);
  const [filters, setFilters] = useState(initialFilters);
  const [loading, setLoading] = useState(false);
  const [recipeLoading, setRecipeLoading] = useState(false);
  const [selectedDish, setSelectedDish] = useState(null);
  const [error, setError] = useState("");
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [showLanding, setShowLanding] = useState(true);
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);
  const [isSidebarMobileOpen, setIsSidebarMobileOpen] = useState(false);

  const handleShowDishes = async () => {
    setError("");
    setSelectedDish(null);
    setLoading(true);
    try {
      const res = await fetch("/api/generate-dishes/ai", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ ingredients }),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || `Failed to generate dishes (${res.status})`);
      }
      const body = await res.json();
      const mapped = (body.dishes || []).map((dish, index) => ({
        id: `${dish.name}-${index}`,
        name: dish.name,
        region: dish.region,
        time: toMinutes(dish.time),
        taste: dish.type || "Veg",
        description: dish.description,
        // recipe will hydrate these later after /generate-recipe call
        ingredients: [],
        steps: [],
      }));
      setDishes(mapped);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to generate dishes");
      setLoading(false);
      return;
    }
    setLoading(false);
  };

  const filteredDishes = useMemo(() => {
    return dishes.filter((dish) => {
      const matchesRegion =
        filters.region.length === 0 || filters.region.includes(dish.region);
      const matchesTime =
        filters.time.length === 0 ||
        filters.time.some((limit) => dish.time <= Number(limit));
      const matchesTaste =
        filters.taste.length === 0 || filters.taste.includes(dish.taste);
      return matchesRegion && matchesTime && matchesTaste;
    });
  }, [dishes, filters]);

  const toggleFilterChip = (group, value) => {
    setFilters((prev) => {
      const exists = prev[group].includes(value);
      return {
        ...prev,
        [group]: exists
          ? prev[group].filter((item) => item !== value)
          : [...prev[group], value],
      };
    });
  };

  const handleViewRecipe = async (dish) => {
    setError("");
    setRecipeLoading(true);
    try {
      const res = await fetch("/api/generate-recipe", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          dishName: dish.name,
          ingredients,
          language: "en",
        }),
      });
      if (!res.ok) {
        const body = await res.json().catch(() => ({}));
        throw new Error(body.message || `Failed to generate recipe (${res.status})`);
      }
      const recipe = await res.json();
      setSelectedDish({
        ...dish,
        description: recipe.recipe || dish.description,
        time: toMinutes(recipe.time || `${dish.time}`),
        ingredients: ingredients,
        steps: Array.isArray(recipe.steps) ? recipe.steps : [],
      });
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to generate recipe");
    } finally {
      setRecipeLoading(false);
    }
  };

  const handleQuickAddIngredient = (ingredient) => {
    const normalized = ingredient.trim();
    if (!normalized) return;

    setIngredients((prev) => {
      const exists = prev.some(
        (current) => current.toLowerCase() === normalized.toLowerCase()
      );
      return exists ? prev : [...prev, normalized];
    });
  };

  if (showLanding) {
    return <LandingPage onGetStarted={() => setShowLanding(false)} />;
  }

  return (
    <div className="dashboard-layout">
      <Sidebar
        collapsed={isSidebarCollapsed}
        onToggleCollapse={() => setIsSidebarCollapsed((prev) => !prev)}
        mobileOpen={isSidebarMobileOpen}
        onCloseMobile={() => setIsSidebarMobileOpen(false)}
      />

      {isSidebarMobileOpen && (
        <button
          type="button"
          aria-label="Close sidebar overlay"
          className="sidebar-backdrop"
          onClick={() => setIsSidebarMobileOpen(false)}
        />
      )}

      <div className={`app-shell ${isSidebarCollapsed ? "collapsed" : ""}`}>
        <div className="container">
          <button
            type="button"
            className="mobile-menu-button"
            onClick={() => setIsSidebarMobileOpen(true)}
          >
            ☰
          </button>
          <header className="top-header" id="top">
            <div className="title-block">
              <h1>🍳 AI Recipe Generator</h1>
            </div>
            <div className="profile-wrap">
              <button
                type="button"
                className="profile-icon-button"
                aria-label="Open profile"
                onClick={() => setIsProfileOpen((prev) => !prev)}
              >
                👤
              </button>

              {isProfileOpen && (
                <div className="profile-card">
                  <div className="profile-top">
                    <div className="profile-avatar">KA</div>
                    <div>
                      <h3>Kaif Ali</h3>
                      <p>Recipe Explorer</p>
                    </div>
                  </div>
                  <div className="profile-meta">
                    <span>Saved Recipes: 12</span>
                    <span>Preferred Taste: Spicy</span>
                  </div>
                  <button type="button" className="profile-action">
                    View Profile
                  </button>
                </div>
              )}
            </div>
          </header>

          <section className="main-split-layout">
            <div className="main-left-content">
              <section className="input-and-filter" id="ingredients">
                <IngredientInput
                  ingredients={ingredients}
                  setIngredients={setIngredients}
                  onShowDishes={handleShowDishes}
                >
                  <FilterChips filters={filters} onToggleFilter={toggleFilterChip} />
                </IngredientInput>
              </section>

              {loading ? (
                <p className="status-text">Finding best dishes for you... 🍳</p>
              ) : (
                <section id="dishes">
                  {error ? <p className="status-text">{error}</p> : null}
                  <DishList
                    dishes={filteredDishes}
                    onViewRecipe={handleViewRecipe}
                    onQuickAddIngredient={handleQuickAddIngredient}
                  />
                </section>
              )}
            </div>

            <RecipeDetailsPanel selectedDish={recipeLoading ? null : selectedDish} />
          </section>
        </div>
      </div>
    </div>
  );
}

export default App;
