import React from "react";

const steps = [
  { icon: "🥕", title: "Add Ingredients" },
  { icon: "🍽️", title: "Show Dishes" },
  { icon: "⚙️", title: "Apply Filters" },
  { icon: "👨‍🍳", title: "View & Cook" },
];

function HowItWorks() {
  return (
    <section className="how-section">
      <h3>How It Works</h3>
      <div className="steps-row">
        {steps.map((step, index) => (
          <div className="step-item" key={step.title}>
            <span className="step-icon">{step.icon}</span>
            <p>{step.title}</p>
            {index < steps.length - 1 && <span className="step-connector">→</span>}
          </div>
        ))}
      </div>
    </section>
  );
}

export default HowItWorks;
