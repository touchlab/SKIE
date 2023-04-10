

import React from "react";
import MacOSWindow from "./MacOSWindow";

import kotlin from '@site/static/samples/suspend/kotlin@2x.png';
import before_skie from '@site/static/samples/suspend/before_skie@2x.png';
import after_skie from '@site/static/samples/suspend/after_skie@2x.png';
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
        icon: "💥",
        title: "Without SKIE",
        contentImage: before_skie,
        background: xcode,
        description: "Calling from background thread crashes",
    },
    {
        icon: "🎉",
        title: "With SKIE",
        contentImage: after_skie,
        background: xcode,
        description: "Cancellable and callable from any thread",
    },
]

export default function SuspendInterop() {
    return (
        <FeatureSubsection
            title="Suspend Interop"
            description="Kotlin suspend functions are converted to Swift's native async functions."
            benefits={[
                "Cancellation support",
                "Callable from any thread",
                "No wrapping necessary",
            ]}
            contentLeft={true}
        >
            <div className="pt-2">
                <MacOSWindow tabs={tabs}/>
            </div>
        </FeatureSubsection>
    )
}
