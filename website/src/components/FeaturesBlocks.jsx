import React from 'react';
import {Circle, Geometry, Link, NetworkConnection, PillBottle} from "./FeatureIcons";
import CenteringSection from "./base/CenteringSection";
import SectionHeader from "./base/SectionHeader";

const colors = {
    background: 'fill-amber-300 dark:fill-amber-400',
    primary: 'stroke-amber-900',
    secondary: 'stroke-amber-600',
}

function DirectLinking() {
    return <Circle colors={colors}>
        <g transform="translate(20 20)">
            <path
                d="M13.4,10.6 L13.4,10.6c2,2,2,5.1,0,7.1l-2.8,2.8c-2,2-5.1,2-7.1,0l0,0c-2-2-2-5.1,0-7.1L6,11"
                className="stroke-current"></path>
            <path
                d="M10.6,13.4L10.6,13.4 c-2-2-2-5.1,0-7.1l2.8-2.8c2-2,5.1-2,7.1,0l0,0c2,2,2,5.1,0,7.1L18,13"
                className="stroke-current text-amber-600"></path>
        </g>
    </Circle>
}

function Flows() {
    return (
        <Circle colors={colors}>
            <g transform="translate(20, 20)">
                <path className={colors.secondary} d="M13.2,15l0.6,0.7 c0.8,0.8,1.8,1.3,3,1.3H22" strokeLinecap="butt"/>
                <path className={colors.secondary} d="M1,7h3.2c1.1,0,2.2,0.5,3,1.3 L7.8,9" strokeLinecap="butt"/>
                <path d="M1,17h3.2c1.1,0,2.2-0.5,3-1.3l6.6-7.4 c0.8-0.8,1.8-1.3,3-1.3H22" strokeLinecap="butt"/>
                <polyline className={colors.secondary} points=" 19,14 22,17 19,20 " stroke="#000000"/>
                <polyline points="19,10 22,7 19,4 "/>
            </g>
        </Circle>
    )
}

function Suspend() {
    return (
        <Circle colors={colors}>
            <g transform="translate(20, 20)">
                <line className={colors.secondary} x1="11" y1="1" x2="11" y2="12" stroke="#000000"/>
                <line x1="1" y1="12" x2="22" y2="12" strokeLinecap="butt"/>
                <polyline points="17,17 22,12 17,7 "/>
                <line className={colors.secondary} x1="11" y1="16" x2="11" y2="23" stroke="#000000"/>
            </g>
        </Circle>
    )
}

function FeatureBlock({name, description, image}) {
    return (
        <div className="relative flex flex-col items-center" data-aos="fade-up" data-aos-anchor="[data-aos-id-blocks]">
            {image}
            <h4 className="h4 mb-2 text-center">{name}</h4>
            <p className="text-lg text-gray-700 dark:text-gray-400 text-center">{description}</p>
        </div>
    )
}

export default function FeaturesBlocks() {
    return (
        <CenteringSection divider={true}>
            <SectionHeader title="Critical features for the KMP iOS developer experience">
                SKIE is packed with features that make Kotlin Multiplatform frameworks feel like native Swift. We carefully designed each feature to be intuitive, while staying consistent throughout many use-cases.
            </SectionHeader>

            {/* Items */}
            <div className="max-w-sm mx-auto grid gap-8 md:grid-cols-3 lg:gap-16 items-start md:max-w-2xl lg:max-w-none" data-aos-id-blocks>
                <FeatureBlock
                    name="Direct Linking"
                    description="The generated Swift is compiled and linked directly to the Xcode Framework. No extra deployment config required."
                    image={<DirectLinking/>}
                />

                <FeatureBlock
                    name="Flows"
                    description="Use any Flow as an AsyncSequence from Swift, keeping the element type intact. Safe and smart."
                    image={<Flows/>}
                />

                <FeatureBlock
                    name="Suspend Interop"
                    description="Call Kotlin suspend functions the same way as Swift's async functions, with cancelation and background thread support."
                    image={<Suspend/>}
                />

                <FeatureBlock
                    name="Transparent Enums"
                    description="Kotlin enums are transparently converted to proper Swift enums. This allows for exhaustive operations on the Swift side."
                    image={<Geometry/>}
                />

                <FeatureBlock
                    name="Sealed Hierarchies"
                    description="Sealed class and sealed interface handling allows you to exhaustively switch on sealed Kotlin hierarchies from Swift."
                    image={<PillBottle/>}
                />

                <FeatureBlock
                    name="Default Parameters"
                    description="Overloaded methods are added to the exposed Swift interface to allow calling methods without specifying each argument."
                    image={<NetworkConnection/>}
                />
            </div>
        </CenteringSection>
    );
}
