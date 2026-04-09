import React from "react";

const filterGroups = [
  {
    key: "region",
    label: "Region",
    options: ["North Indian", "South Indian", "Street Food"],
  },
  {
    key: "taste",
    label: "Taste",
    options: ["Mild", "Medium", "Spicy"],
  },
  {
    key: "time",
    label: "Time",
    options: ["10", "20", "30"],
  },
];

function FilterChips({ filters, onToggleFilter }) {
  return (
    <section className="filter-chips-section">
      {filterGroups.map((group) => (
        <div className="filter-chip-group" key={group.key}>
          <p>{group.label}</p>
          <div className="filter-chip-row">
            {group.options.map((option) => {
              const value = group.key === "time" ? option : option;
              const isActive = filters[group.key].includes(value);
              return (
                <button
                  key={option}
                  type="button"
                  className={`filter-chip ${isActive ? "active" : ""}`}
                  onClick={() => onToggleFilter(group.key, value)}
                >
                  {group.key === "time" ? `${option} min` : option}
                </button>
              );
            })}
          </div>
        </div>
      ))}
    </section>
  );
}

export default FilterChips;
