import React from 'react';

import HeroImage from '@site/static/componentimg/hero.jpg';

export default function HeroAbout() {
    return (
        <section className="relative bg-slate-100 dark:bg-slate-800">
            {/*<div className="absolute inset-0">*/}
            {/*    <img className="w-full h-full object-cover" src={HeroImage} width="1440" height="394" alt="About"/>*/}
            {/*    <div className="absolute inset-0 bg-blue-900 opacity-50" aria-hidden="true"></div>*/}
            {/*</div>*/}

            {/* Hero content */}
            <div className="max-w-5xl mx-auto px-4 sm:px-6 relative text-start">
                <div className="pt-8 pb-12 md:pt-24 md:pb-28">
                    <div className="max-w-5xl mx-auto mb-8">
                        <h1 className="h1 font-normal md:text-7xl md:leading-none" data-aos="fade-up">
                            Elevate your KMM<br/> experience with SKIE
                        </h1>
                    </div>
                    {/* Section header */}
                    <div className="max-w-5xl mx-auto mb-8 md:mb-16 ">
                        <h4 className="h4 max-w-3xl mb-4">A Swift-friendly API Generator for Kotlin Multiplatform Mobile</h4>
                        <p className="max-w-3xl text-xl text-gray-700 dark:text-gray-400">Don’t let the lack of proper Swift interop hamper your Kotlin Multiplatform adoption. With SKIE, you can restore the expressiveness of modern languages and enjoy a&nbsp;superior iOS developer experience.</p>
                    </div>
                    <div className="max-w-xs mx-auto sm:max-w-none flex justify-start flex-col md:flex-row gap-4" data-aos="fade-up" data-aos-delay="400">
                        <a className="btn font-semibold text-lg text-gray-700 bg-amber-300 hover:bg-amber-200 sm:w-auto drop-shadow-lg no-underline"
                           href="#demo">Book a Demo</a>

                        <a className="btn border-2 font-semibold text-lg border-solid border-cyan-600 hover:bg-cyan-600 hover:text-white box-border no-underline" href="intro">Explore Docs</a>
                    </div>
                </div>
            </div>

        </section>
    );
}
