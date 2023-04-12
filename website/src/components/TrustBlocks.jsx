import React from 'react';
import {Circle, Geometry, Link, NetworkConnection, PillBottle} from "./FeatureIcons";
import ImageTitleBlock from "./ImageTitleBlock";
import CenteringSection from "./base/CenteringSection";
import SectionHeader from "./base/SectionHeader";

const colors = {
    background: "fill-touchlab-emerald",
    primary: "stroke-emerald-900",
    secondary: "stroke-emerald-600",
};

function ComprehensiveTesting() {
    return (
        <Circle colors={colors}>
            <g transform="translate(19, 19)">
                <path className={colors.secondary} d="M18,5V1H6V5Z"/>
                <path d="M9,8v5L4.249,18.938A2.5,2.5,0,0,0,6.2,23H17.8a2.5,2.5,0,0,0,1.953-4.062L15,13V8"/>
            </g>
        </Circle>
    )
}

function ImmenseFlexibility() {
    return (
        <Circle colors={colors}>
            <g transform="translate(20, 20)">
                <line x1="4" y1="10" x2="4" y2="1"/>
                <line x1="4" y1="23" x2="4" y2="20"/>
                <line className={colors.secondary} x1="12" y1="2" x2="12" y2="1"/>
                <line className={colors.secondary} x1="12" y1="23" x2="12" y2="12"/>
                <line x1="20" y1="10" x2="20" y2="1"/>
                <line x1="20" y1="23" x2="20" y2="20"/>
                <circle cx="4" cy="17" r="3"/>
                <circle className={colors.secondary} cx="12" cy="9" r="3"/>
                <circle cx="20" cy="17" r="3"/>
            </g>
        </Circle>
    )
}

function MadeByTouchlab() {
    return (
        <Circle colors={colors}>
            <g className='origin-center' transform="scale(0.9) translate(16, 19)">
                <path className={colors.secondary} d="M19.5,21.5l2.25,2.25a2.476,2.476,0,0,1,0,3.5h0" />
                <path className={colors.secondary} d="M1,14V2H11.1a7.081,7.081,0,0,1,.9.058" />
                <rect x="2.422" y="15.775" width="5.657" height="4.95" rx="2.475" ry="2.475" transform="translate(-11.367 9.058) rotate(-45)"/>
                <rect x="5.727" y="19.275" width="6.046" height="4.95" rx="2.475" ry="2.475" transform="translate(-12.817 12.558) rotate(-45)"/>
                <rect x="9.286" y="22.911" width="5.657" height="4.95" rx="2.475" ry="2.475" transform="translate(-14.402 16.001) rotate(-45)"/>
                <rect x="12.905" y="26.485" width="4.977" height="4.243" rx="2.121" ry="2.121" transform="translate(-15.719 19.264) rotate(-45)"/>
                <path className={colors.secondary} d="M23,18l3.025,3.025a2.474,2.474,0,0,1,0,3.5h0a2.474,2.474,0,0,1-3.5,0" />
                <path className={colors.secondary} d="M25.75,20.75a2.476,2.476,0,0,0,3.5,0h0a2.476,2.476,0,0,0,0-3.5L20,8" strokeLinecap="butt" />
                <path d="M20,8l-6.9,5.173a2.232,2.232,0,0,1-2.336.211h0a2.23,2.23,0,0,1-.579-3.572L15.95,4.05A7,7,0,0,1,20.9,2H31V14"/>
            </g>
        </Circle>
    )
}

export default function TrustBlocks() {
    return (
        <CenteringSection divider={true} background="bg-slate-50">
            <SectionHeader title="Risk Mitigation">
                Adopting Kotlin Multiplatform can be challenging and introducing a new tool to your team might be daunting. Low entry barrier has been part of SKIE from the beginning.
            </SectionHeader>

            <div
                className="max-w-sm mx-auto grid gap-8 md:grid-cols-3 lg:gap-16 items-start md:max-w-2xl lg:max-w-none"
                data-aos-id-blocks>

                <ImageTitleBlock image={<ComprehensiveTesting/>} title="Comprehensive Testing">
                    We test SKIE against a <strong>thousand</strong> public KMP libraries along
                    with <strong>hundreds</strong> of hand-written tests.
                </ImageTitleBlock>

                <ImageTitleBlock image={<ImmenseFlexibility/>} title="Immense Flexibility">
                    No two projects are the same. SKIE has granular configurability, letting your team decide what to enhance.
                </ImageTitleBlock>

                <ImageTitleBlock image={<MadeByTouchlab/>} title="Made by Touchlab">
                    Touchlab has years of experience with Kotlin Multiplatform in production. We built SKIE to solve our problems, let it solve yours too.
                </ImageTitleBlock>
            </div>
        </CenteringSection>
    )
}
