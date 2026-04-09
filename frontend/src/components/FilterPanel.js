import React, { useState } from "react";

const regionOptions = ["North Indian", "South Indian"];
const timeOptions = [15, 20, 25, 30];
const tasteOptions = ["Spicy", "Medium", "Mild", "Tangy"];

function FilterPanel({ filters, onApply, onReset, onClose }) {
  const [localFilters, setLocalFilters] = useState(filters);

  const updateFilter = (field, value) => {
    setLocalFilters((prev) => ({ ...prev, [field]: value }));
  };

  const handleApply = () => {
    onApply(localFilters);
  };

  const handleReset = () => {
    const resetState = { region: "", time: "", taste: "" };
    setLocalFilters(resetState);
    onReset();
  };

  return (
    <div className="overlay">
      <div className="panel">
        <div className="panel-header">
          <h2>Filter Dishes</h2>
          <button type="button" className="close-button" onClick={onClose}>
            ✖
          </button>
        </div>

        <label htmlFor="region-select">Region</label>
        <select
          id="region-select"
          value={localFilters.region}
          onChange={(event) => updateFilter("region", event.target.value)}
        >
          <option value="">All Regions</option>
          {regionOptions.map((region) => (
            <option key={region} value={region}>
              {region}
            </option>
          ))}
        </select>

        <label htmlFor="time-select">Time</label>
        <select
          id="time-select"
          value={localFilters.time}
          onChange={(event) => updateFilter("time", event.target.value)}
        >
          <option value="">Any Time</option>
          {timeOptions.map((time) => (
            <option key={time} value={time}>
              Under {time} min
            </option>
          ))}
        </select>

        <label htmlFor="taste-select">Taste</label>
        <select
          id="taste-select"
          value={localFilters.taste}
          onChange={(event) => updateFilter("taste", event.target.value)}
        >
          <option value="">All Tastes</option>
          {tasteOptions.map((taste) => (
            <option key={taste} value={taste}>
              {taste}
            </option>
          ))}
        </select>

        <div className="panel-actions">
          <button type="button" className="apply-button" onClick={handleApply}>
            Apply Filters
          </button>
          <button type="button" className="reset-button" onClick={handleReset}>
            Reset Filters
          </button>
        </div>
      </div>
    </div>
  );
}

export default FilterPanel;
