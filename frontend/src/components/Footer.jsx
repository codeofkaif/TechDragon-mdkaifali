import React from "react";

function Footer({ onGetStarted }) {
  return (
    <footer className="landing-footer">
      <p>Made with ❤️ by Mojammil Husain</p>
      <button type="button" className="hero-cta" onClick={onGetStarted}>
        Start Cooking Now!
      </button>
    </footer>
  );
}

export default Footer;
