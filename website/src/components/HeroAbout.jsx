import React from 'react';

import HeroImage from '@site/static/componentimg/hero.jpg';

function HeroAbout() {
  return (
    <section className="relative">

      {/* Background image */}
      <div className="absolute inset-0">
        <img className="w-full h-full object-cover" src={HeroImage} width="1440" height="394" alt="About" />
        <div className="absolute inset-0 bg-blue-900 opacity-50" aria-hidden="true"></div>
      </div>

      {/* Hero content */}
      <div className="max-w-6xl mx-auto px-4 sm:px-6 relative">
        <div className="pt-32 pb-12 md:pt-52 md:pb-28">
          <div className="max-w-3xl mx-auto text-center">
            <h1 className="h1 mb-4 drop-shadow-lg text-white" data-aos="fade-up">SKIE</h1>
            <h2 className="h2 mb-4 drop-shadow-lg text-white" data-aos="fade-up">Swift/Kotlin Interface Extender</h2>
          </div>
            <div className="max-w-xs mx-auto sm:max-w-none sm:flex sm:justify-center">
                <div data-aos="fade-up" data-aos-delay="400">
                    <a className="btn text-white bg-cyan-600 hover:bg-cyan-700 w-full mb-4 sm:w-auto sm:mb-0 drop-shadow-lg" href="intro">Get Started</a>
                </div>
            </div>
        </div>
      </div>

    </section>
  );
}

export default HeroAbout;