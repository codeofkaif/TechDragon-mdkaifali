import React from "react";

function HeroSection({ onGetStarted }) {
  return (
    <section className="hero-section">
      <div className="hero-left">
        <h2>Turn your ingredients into delicious recipes instantly!</h2>
        <p>
          Discover tasty dishes with the ingredients you have at home.
        </p>
        <button type="button" className="hero-cta" onClick={onGetStarted}>
          Get Started
        </button>
      </div>

      <div className="hero-right">
        <div className="mock-laptop">
          <div className="mock-topbar">
            <span />
            <span />
            <span />
          </div>
          <div className="mock-content">
            <div className="mock-chips">
              <span>Potato</span>
              <span>Tomato</span>
              <span>Onion</span>
            </div>
            <div className="mock-dishes">
              <article>
                <h4>Aloo Jeera</h4>
                <p>Spicy, North Indian</p>
              </article>
              <article>
                <h4>Masala Fry</h4>
                <p>Crispy and quick</p>
              </article>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

export default HeroSection;
