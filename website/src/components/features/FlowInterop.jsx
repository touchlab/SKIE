import React from "react";
import MacOSWindow from "./MacOSWindow";

import kotlin from '@site/static/samples/flows/kotlin@2x.png';
import before_skie from '@site/static/samples/flows/before_skie@2x.png';
import after_skie from '@site/static/samples/flows/after_skie@2x.png';
import {checkMark} from "./Icons";
import {idea, xcode} from "./CodeBackgrounds";
import FeatureSubsection from "./FeatureSubsection";

const tabs = [
    {
        icon: "🧑‍💻",
        title: "Kotlin",
        contentImage: kotlin,
        background: idea,
        description: "No changes on Kotlin side"
    },
    {
        icon: "😭",
        title: "Without SKIE",
        contentImage: before_skie,
        background: xcode,
        description: "Flow API is lost",
    },
    {
        icon: "🎉",
        title: "With SKIE",
        contentImage: after_skie,
        background: xcode,
        description: "Native async sequence behavior!",
    },
]

export default function FlowInterop() {
    return (
        <FeatureSubsection
            title="Flow Support"
            description="Kotlin Flows are automatically and transparently converted to Swift AsyncSequences."
            benefits={[
                "Proper compile-time checking",
                "Swift-native ergonomics",
                "Flow, StateFlow and MutableStateFlow",
            ]}
        >
            <MacOSWindow tabs={tabs}/>
        </FeatureSubsection>
    )
}
