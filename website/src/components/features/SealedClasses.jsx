import React from "react";
import MacOSWindow from "./MacOSWindow";
import { idea, xcode } from "./CodeBackgrounds";
import { checkMark } from "./Icons";

import sealedKotlin from '@site/static/samples/sealed/kotlin@2x.png';
import sealedSwiftBefore from '@site/static/samples/sealed/before_skie@2x.png';
import sealedSwiftAfter from '@site/static/samples/sealed/after_skie@2x.png';
import sealedSwiftAfterComplete from '@site/static/samples/sealed/after_skie_complete@2x.png';
import Subsection from "../base/Subsection";
import FeatureSubsection from "./FeatureSubsection";

const tabs = [
    {
        icon: "🧑‍💻",
        title: "Kotlin",
        contentImage: sealedKotlin,
        background: idea,
        description: "Sealed classes are ever-present",
    },
    {
        icon: "😭",
        title: "Without SKIE",
        contentImage: sealedSwiftBefore,
        background: xcode,
        description: "No compile-time exhaustive checking"
    },
    {
        icon: "🎉",
        title: "With SKIE",
        contentImage: sealedSwiftAfter,
        background: xcode,
        description: "Xcode now knows which cases are missing",
    },
    /*{
        icon: "🎉",
        title: "With SKIE",
        contentImage: sealedSwiftAfterComplete,
        background: xcode,
        description: "... and Xcode missing cases support.",
    },*/
]

export default function SealedClasses() {
    return (
        <FeatureSubsection
            title="Sealed class wrapped as an enum"
            description="Sealed classes are unchanged, but an associated enum is generated, as well as a wrapper function to use in switch statements."
            benefits={[
                "Exhaustively checked sealed classes",
                "Similar to enums with associated values",
            ]}
        >
            <div className="pt-2">
                <MacOSWindow tabs={tabs}/>
            </div>
        </FeatureSubsection>
    )
}
