import React from "react";
import HeroSection from "./HeroSection.jsx";
import FeaturesSection from "./FeaturesSection.jsx";
import HowItWorks from "./HowItWorks.jsx";
import RecipePreview from "./RecipePreview.jsx";
import Footer from "./Footer.jsx";

function LandingPage({ onGetStarted }) {
  return (
    <div className="landing-shell">
      <div className="landing-container">
        <HeroSection onGetStarted={onGetStarted} />
        <FeaturesSection />
        <HowItWorks />
        <RecipePreview />
        <Footer onGetStarted={onGetStarted} />
      </div>
    </div>
  );
}

export default LandingPage;
