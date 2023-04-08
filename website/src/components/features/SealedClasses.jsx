import {AndroidOnly, ThumbDownTab, ThumbUpTab} from "../FeatureIcons";
import React from "react";
import MacOSWindow from "./MacOSWindow";
import { idea, xcode } from "./CodeBackgrounds";
import { checkMark } from "./Icons";

import sealedKotlin from '@site/static/samples/sealed/kotlin@2x.png';
import sealedSwiftBefore from '@site/static/samples/sealed/before_skie@2x.png';
import sealedSwiftAfter from '@site/static/samples/sealed/after_skie@2x.png';
import sealedSwiftAfterComplete from '@site/static/samples/sealed/after_skie_complete@2x.png';

export default function SealedClasses() {
    return <div className="py-16 border-0 border-t border-solid border-slate-100">
        <div className="mx-auto grid gap-8 md:grid-cols-feature-left items-center">
            <div className="items-center">
                <h3 className="h3 mb-3">Sealed class wrapped as an enum</h3>
                <p className="text-xl text-gray-700 dark:text-gray-400 mb-4">Sealed classes are unchanged, but an associated
                    enum is generated, as well as a wrapper function to use in switch statements.</p>
                <ul className="text-lg text-gray-700 dark:text-gray-400 -mb-2 p-0">
                    <li className="flex items-center mb-2">
                        {checkMark("lime")}
                        <span>Exhaustively checked sealed classes</span>
                    </li>
                    <li className="flex items-center mb-2">
                        {checkMark("lime")}
                        <span>Similar to enums with associated values</span>
                    </li>
                </ul>
            </div>

            <div className="pt-2">
                <MacOSWindow tabs={[
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
                        description: "Compile-time safety missing"
                    },
                    {
                        icon: "😊",
                        title: "With SKIE",
                        contentImage: sealedSwiftAfter,
                        background: xcode,
                        description: "Sealed classes as enums with exhaustive checking ..",
                    },
                    {
                        icon: "🎉",
                        title: "With SKIE",
                        contentImage: sealedSwiftAfterComplete,
                        background: xcode,
                        description: "... and Xcode missing cases support.",
                    },
                ]}/>
            </div>
        </div>
    </div>
}
