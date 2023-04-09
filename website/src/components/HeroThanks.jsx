import React from 'react';

import HeroImage from '@site/static/componentimg/hero.jpg';

export default function HeroThanks() {
    return (
        <section className="relative bg-slate-100 dark:bg-slate-800">
            <div className="max-w-5xl mx-auto px-4 sm:px-6 relative text-start">
                <div className="pt-8 pb-12 md:pt-24 md:pb-28">
                    <div className="max-w-5xl mx-auto mb-8">
                        <h1 className="h1 font-normal md:text-7xl md:leading-none" data-aos="fade-up">
                            Thank you for trying SKIE!
                        </h1>
                    </div>
                    {/* Section header */}
                    <div className="max-w-5xl mx-auto mb-8 md:mb-16 ">
                        <h4 className="h4 max-w-3xl mb-4">Check your email for next steps</h4>
                        <p className="max-w-3xl text-xl text-gray-700 dark:text-gray-400">We really could not be more excited to show you and the world what we've been working on.
                            Please let us know what you think, and anything else that would help your team
                            be more efficient and successful!</p>
                        <p className="max-w-3xl text-xl text-gray-700 dark:text-gray-400">In the meantime, take a look at the SKIE documentation to get started.</p>
                    </div>
                    <div className="max-w-xs mx-auto sm:max-w-none flex justify-start sm:flex-col md:flex-row sm:space-y-4 md:space-y-0 md:space-x-4" data-aos="fade-up" data-aos-delay="400">
                        <a className="btn border-2 border-solid border-cyan-600 hover:bg-cyan-600 hover:text-white box-border no-underline" href="intro">Explore Docs</a>
                    </div>
                </div>
            </div>
        </section>
    );
}
