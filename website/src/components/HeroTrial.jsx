import React from 'react';
import CenteringSection from "./base/CenteringSection";

export default function HeroTrial() {
    return (
        <CenteringSection
            background='bg-slate-100 dark:bg-slate-800'
            outerPadding='pt-20 pb-12 md:pt-40 md:pb-28'
        >
            <div className="max-w-5xl mx-auto mb-8">
                <h1 className="h1 font-normal md:text-7xl md:leading-none" data-aos="fade-up">
                    Free SKIE Trial<br/>for KotlinConf VIPs
                </h1>
            </div>
            {/* Section header */}
            <div className="max-w-5xl mx-auto mb-8 md:mb-16 ">
                {/*<h4 className="h4 max-w-3xl mb-4">It was great meeting you at KotlinConf this year and we’re so glad you were excited about SKIE. Enjoy your free trial, we’ll be in touch!</h4>*/}
                <p className="max-w-3xl text-xl text-gray-700 dark:text-gray-400">It was great meeting you at KotlinConf this year and we’re
                    so glad you were excited about SKIE. Enjoy your free trial, we’ll be in touch!</p>
            </div>
        </CenteringSection>
    )
}
