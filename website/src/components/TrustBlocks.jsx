import React from 'react';
import {Circle, Geometry, Link, NetworkConnection, PillBottle} from "./FeatureIcons";
import ImageTitleBlock from "./ImageTitleBlock";

const colors = {
    background: "fill-touchlab-emerald",
    primary: "stroke-emerald-900",
    secondary: "stroke-emerald-600",
};

function ComprehensiveTesting() {
    return (
        <Circle colors={colors}>
            <g className='origin-center' transform="scale(0.55)">
                <path d="M26,15V31L11.86,53.325A5,5,0,0,0,16.084,61h31.83a5,5,0,0,0,4.224-7.676L38,31V15" fill="none" />
                <rect height="7" width="28" fill="none" x="18" y="3"/>
                <circle className={colors.secondary} cx="26" cy="51" fill="none" r="4" />
                <circle className={colors.secondary} cx="33" cy="40" fill="none" r="3" />
                <circle className={colors.secondary} cx="40" cy="52" fill="none" r="2" />
            </g>
        </Circle>
    )
}

function ImmenseFlexibility() {
    return (
        <Circle colors={colors}>
            <g className='origin-center' transform="scale(0.55)">
                <line x1="12" y1="30" x2="12" y2="3"/>
                <line x1="12" y1="61" x2="12" y2="52"/>
                <circle className={colors.secondary} cx="12" cy="44" r="8"/>
                <line x1="52" y1="30" x2="52" y2="3"/>
                <line x1="52" y1="61" x2="52" y2="52"/>
                <circle className={colors.secondary} cx="52" cy="44" r="8"/>
                <line x1="32" y1="61" x2="32" y2="34"/>
                <line x1="32" y1="12" x2="32" y2="3"/>
                <circle className={colors.secondary} cx="32" cy="20" r="8"/>
            </g>
        </Circle>
    )
}

function MadeByTouchlab() {
    return (
        <Circle colors={colors}>
            <g className='origin-center' transform="scale(0.55)">
                <path className={colors.secondary} d="M48.044,38.171l3.809,3.808a3.929,3.929,0,0,1,0,5.554h0a3.928,3.928,0,0,1-5.555,0l-3.807-3.806"/>
                <path className={colors.secondary} d="M36.974,49.244l1.787,1.788a3.9,3.9,0,0,1,0,5.515h0A3.888,3.888,0,0,1,36,57.689"/>
                <polyline className={colors.secondary} points="7 29 7 23 3 19 17 5 20 8 25 8"/>
                <rect x="10.254" y="39.731" width="9.312" height="7.855" rx="3.928" ry="3.928" transform="translate(-26.504 23.331) rotate(-45)"/>
                <rect x="22.565" y="49.937" width="8.544" height="7.8" rx="3.9" ry="3.9" transform="translate(-30.208 34.745) rotate(-45)"/>
                <path d="M12.631,40.383l-.58.58a3.794,3.794,0,0,1-5.5-.136h0a3.8,3.8,0,0,1,.092-5.195l.58-.593a3.8,3.8,0,0,1,5.343-.091h0A3.8,3.8,0,0,1,12.631,40.383Z"/>
                <rect x="16.731" y="44.906" width="8.212" height="7.8" rx="3.9" ry="3.9" transform="translate(-28.408 29.029) rotate(-45)"/>
                <path className={colors.secondary} d="M45.537,46.776a3.9,3.9,0,0,1,0,5.516h0a3.9,3.9,0,0,1-5.515,0L38.73,51"/>
                <path className={colors.secondary} d="M50.776,40.9a3.8,3.8,0,0,0,5.5-.136h0a3.8,3.8,0,0,0-.092-5.195L43.476,22.51"/>
                <path d="M57,29V23l4-4L47,5,44,8H37.447a12.067,12.067,0,0,0-8.534,3.535l-9.206,9.571a3.4,3.4,0,0,0,4.349,5.185l9.8-6.318a8.908,8.908,0,0,0,13.483-.14"/>
            </g>
        </Circle>
    )
}

export default function TrustBlocks() {
    return (
        <section className="bg-slate-50">

            <div className="max-w-5xl mx-auto px-4 sm:px-6 py-16 sm:px-6 border-0 border-solid border-t border-slate-100">
                <div className="border-t border-gray-800">
                    {/* Section header */}
                    <div className="max-w-3xl mx-auto text-center">
                        <h2 className="h2 mb-4">Risk Mitigation</h2>
                        <p className="text-xl text-gray-700 dark:text-gray-400">
                            Adopting Kotlin Multiplatform can be challenging and introducing a new tool to your team might be daunting. Low entry barrier has been part of SKIE from the beginning.
                        </p>
                    </div>
                </div>

                <div className="border-t border-gray-800 pt-4">

                    {/* Items */}
                    <div
                        className="max-w-sm mx-auto grid gap-8 md:grid-cols-3 lg:gap-16 items-start md:max-w-2xl lg:max-w-none"
                        data-aos-id-blocks>

                        <ImageTitleBlock image={<ComprehensiveTesting/>} title="Comprehensive Testing">
                            We test SKIE against a <strong>thousand</strong> public KMM libraries along
                            with <strong>hundreds</strong> of hand-written tests.
                        </ImageTitleBlock>

                        <ImageTitleBlock image={<ImmenseFlexibility/>} title="Immense Flexibility">
                            No two projects are the same. SKIE has granular configurability, letting your team decide what to enhance.
                        </ImageTitleBlock>

                        <ImageTitleBlock image={<MadeByTouchlab/>} title="Made by Touchlab">
                            Touchlab has years of experience with Kotlin Multiplatform in production. We built SKIE to solve our problems, let it solve yours too.
                        </ImageTitleBlock>
                    </div>
                </div>
            </div>
        </section>
    );
}
