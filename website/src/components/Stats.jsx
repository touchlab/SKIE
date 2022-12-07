import React from 'react';

function Stats() {
  return (
    <section>
      <div className="max-w-6xl mx-auto px-4 sm:px-6">
        <div className="pt-10 pb-12 md:pt-16 md:pb-20">

          {/* Section header */}
          <div className="max-w-3xl mx-auto text-center pb-12 md:pb-16">
            <h1 className="h2 mb-4">Integrated workflow designed for product teams</h1>
            <p className="text-xl text-gray-700 dark:text-gray-400">Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia
                deserunt mollit laborum — semper quis lectus nulla.</p>
          </div>

          <div className="grid md:grid-cols-3 bg-gray-100 dark:bg-gray-800 divide-y md:divide-y-0 md:divide-x divide-gray-700 px-6 md:px-0 md:py-8 text-center">
            {/* 1st item */}
            <div className="py-6 md:py-0 md:px-8">
              <div className="text-4xl font-bold leading-tight tracking-tighter text-cyan-600 dark:text-cyan-300 mb-2" data-aos="fade-up">179K</div>
              <div className="text-lg text-gray-700 dark:text-gray-400" data-aos="fade-up" data-aos-delay="200">Lorem ipsum is placeholder text commonly used.</div>
            </div>
            {/* 2nd item */}
            <div className="py-6 md:py-0 md:px-8">
              <div className="text-4xl font-bold leading-tight tracking-tighter text-cyan-600 dark:text-cyan-300 mb-2" data-aos="fade-up">147%</div>
              <div className="text-lg text-gray-700 dark:text-gray-400" data-aos="fade-up" data-aos-delay="200">Lorem ipsum is placeholder text commonly used.</div>
            </div>
            {/* 3rd item */}
            <div className="py-6 md:py-0 md:px-8">
              <div className="text-4xl font-bold leading-tight tracking-tighter text-cyan-600 dark:text-cyan-300 mb-2" data-aos="fade-up">$97K</div>
              <div className="text-lg text-gray-700 dark:text-gray-400" data-aos="fade-up" data-aos-delay="200">Lorem ipsum is placeholder text commonly used.</div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

export default Stats;