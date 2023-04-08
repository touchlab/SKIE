import {AndroidOnly, ThumbDownTab, ThumbUpTab} from "../FeatureIcons";
import React from "react";
import MacOSWindow from "./MacOSWindow";

import kotlin from '@site/static/samples/flows/kotlin@2x.png';
import before_skie from '@site/static/samples/flows/before_skie@2x.png';
import after_skie from '@site/static/samples/flows/after_skie@2x.png';
import {checkMark} from "./Icons";
import {idea, xcode} from "./CodeBackgrounds";

export default function FlowInterop() {
    return (<div className="py-16 mx-auto grid md:grid-cols-feature-left items-center gap-8">

        <div className="items-center">
            <h3 className="h3 mb-3">Flow Support</h3>
            <p className="text-xl text-gray-700 dark:text-gray-400 mb-4">Kotlin Flows are automatically and transparently
                converted to Swift AsyncSequences.</p>
            <ul className="text-lg text-gray-700 dark:text-gray-400 -mb-2 p-0">
                <li className="flex items-center mb-2">
                    {checkMark("lime")}
                    <span>Proper compile-time checking</span>
                </li>
                <li className="flex items-center mb-2">
                    {checkMark("lime")}
                    <span>Swift-native ergonomics</span>
                </li>
                <li className="flex items-center mb-2">
                    {checkMark("lime")}
                    <span>Flow, StateFlow and MutableStateFlow</span>
                </li>
            </ul>
        </div>

        {/* 1st item */}

        <div className="pt-2">
            <MacOSWindow tabs={[
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
            ]}/>
        </div>
    </div>)
}
