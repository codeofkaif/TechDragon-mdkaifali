import React from "react";

const features = [
  {
    icon: "🥕",
    title: "Add Ingredients",
    description: "Enter what you have",
  },
  {
    icon: "🍲",
    title: "Get Recipes",
    description: "AI suggests dishes",
  },
  {
    icon: "🎯",
    title: "Apply Filters",
    description: "Filter by cuisine & time",
  },
  {
    icon: "👨‍🍳",
    title: "View & Cook",
    description: "Follow easy steps",
  },
];

function FeaturesSection() {
  return (
    <section className="features-section">
      {features.map((feature) => (
        <article className="feature-card" key={feature.title}>
          <span className="feature-icon">{feature.icon}</span>
          <h3>{feature.title}</h3>
          <p>{feature.description}</p>
        </article>
      ))}
    </section>
  );
}

export default FeaturesSection;
