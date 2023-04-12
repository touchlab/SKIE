import React from 'react';

import HeroImage from '@site/static/componentimg/hero.jpg';
import CenteringSection from "./base/CenteringSection";

export default function HeroAbout() {
    return (
        <CenteringSection
            background='bg-slate-100 dark:bg-slate-800'
            outerPadding='pt-20 pb-12 md:pt-40 md:pb-28'
        >
            <div className="max-w-5xl mx-auto mb-8">
                <h1 className="h1 font-normal md:text-7xl md:leading-none" data-aos="fade-up">
                    Elevate your KMP<br/> experience with SKIE
                </h1>
            </div>

            <div className="max-w-5xl mx-auto mb-8 md:mb-16 ">
                <h4 className="h4 max-w-3xl mb-4">A Swift-friendly API Generator for Kotlin Multiplatform</h4>
                <p className="max-w-3xl text-xl text-gray-700 dark:text-gray-400">Don’t let the lack of proper Swift interop hinder your Kotlin Multiplatform adoption. With SKIE, you can restore the expressiveness of modern languages and enjoy a&nbsp;superior iOS developer experience.</p>
            </div>
            <div className="max-w-xs mx-auto sm:max-w-none flex justify-start flex-col md:flex-row gap-4" data-aos="fade-up" data-aos-delay="400">
                <a className="btn font-semibold text-lg text-gray-700 bg-amber-300 hover:bg-amber-400 sm:w-auto no-underline"
                   href="#demo">Book a Demo</a>

                <a className="btn border-2 font-semibold text-lg border-solid border-cyan-600 hover:bg-cyan-600 hover:text-white box-border no-underline" href="intro">Explore Docs</a>
            </div>
        </CenteringSection>
    )
}
