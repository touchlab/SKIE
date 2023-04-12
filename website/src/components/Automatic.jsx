import React from 'react';
import {Circle} from "./FeatureIcons";
import CenteringSection from "./base/CenteringSection";
import SectionHeader from "./base/SectionHeader";

const colors = {
    background: 'fill-blue-600',
    primary: 'stroke-blue-100',
    secondary: 'stroke-blue-100',
}

function License() {
    return (
        <Circle colors={colors}>
            <g transform="translate(20, 20)">
                <path d="M18,1L9.766,9.234 C9.201,9.086,8.611,9,8,9c-3.866,0-7,3.134-7,7c0,3.866,3.134,7,7,7s7-3.134,7-7c0-0.891-0.173-1.74-0.476-2.524L17,11V8h3l3-3V1H18 z"/>
                <circle className={colors.secondary} cx="8" cy="16" r="2" />
            </g>
        </Circle>
    )
}

function Plugin() {
    return (
        <Circle colors={colors}>
            <g transform='translate(20, 20)'>
                <line x1="2" y1="22" x2="4.464" y2="19.536"/>
                <path d="M5.964,10.964l-1.5,1.5a5,5,0,0,0,7.072,7.072l1.5-1.5Z"/>
                <line className={colors.secondary} x1="22" y1="2" x2="19.536" y2="4.464"/>
                <path className={colors.secondary} d="M18.036,13.036l1.5-1.5a5,5,0,0,0-7.072-7.072l-1.5,1.5Z"/>
                <line x1="10" y1="11" x2="8" y2="13"/>
                <line x1="13" y1="14" x2="11" y2="16"/>
            </g>
        </Circle>
    )
}

function Profit() {
    return (
        <Circle colors={colors}>
            <g transform='translate(20, 20)'>
                <polyline points="12 9 15 9 9 2 3 9 6 9 6 23 9 23"/>
                <circle className={colors.secondary} cx="14" cy="14" r="2" />
                <circle className={colors.secondary} cx="21" cy="21" r="2" />
                <line className={colors.secondary} x1="13" y1="22" x2="22" y2="13" />
            </g>
        </Circle>
    )
}

export default function Automatic() {
    return (
        <CenteringSection divider={true}>
            <SectionHeader title="Zero-Effort Integration">
                With SKIE, there's no need for additional development effort or training. This powerful tool automatically analyzes your code and modifies the API surface to streamline your workflow. Getting started is as easy as following three simple steps:
            </SectionHeader>


            {/* Items */}
            <div className="max-w-sm mx-auto grid gap-8 md:grid-cols-3 lg:gap-16 items-start md:max-w-none">

                {/* 1st item */}
                <div className="relative flex flex-col items-center" data-aos="fade-up">
                    <div aria-hidden="true" className="absolute h-1 border-0 border-t border-dashed border-blue-400 hidden md:block" style={{ width: 'calc(100% - 32px)', left: 'calc(50% + 48px)', top: '32px' }} data-aos="fade-in" data-aos-delay="200"></div>
                    <License/>
                    <h4 className="h4 mb-2"><span className="text-gray-400">1.</span> License</h4>
                    <p className="text-lg text-gray-700 text-center">Apply Touchlab's licensing plugin in your Gradle Settings file and give it your access key.</p>
                </div>

                {/* 2nd item */}
                <div className="relative flex flex-col items-center" data-aos="fade-up" data-aos-delay="200">
                    <div aria-hidden="true" className="absolute h-1 border-0 border-t border-dashed border-blue-400 hidden md:block" style={{ width: 'calc(100% - 32px)', left: 'calc(50% + 48px)', top: '32px' }} data-aos="fade-in" data-aos-delay="400"></div>
                    <Plugin/>
                    <h4 className="h4 mb-2"><span className="text-gray-400">2.</span> Plugin</h4>
                    <p className="text-lg text-gray-700 text-center">Add SKIE Gradle plugin to your shared module, that produces a framework for iOS.</p>
                </div>

                {/* 3rd item */}
                <div className="relative flex flex-col items-center" data-aos="fade-up" data-aos-delay="400">
                    <Profit/>
                    <h4 className="h4 mb-2"><span className="text-gray-400">3.</span> Profit</h4>
                    <p className="text-lg text-gray-700 text-center">You and your team are ready to enjoy Swift-friendly and compile-time safe APIs.</p>
                </div>
            </div>
        </CenteringSection>
    )
}
