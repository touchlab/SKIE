

import React from "react";
import MacOSWindow from "./MacOSWindow";

import kotlin from '@site/static/samples/suspend/kotlin@2x.png';
import before_skie from '@site/static/samples/suspend/before_skie@2x.png';
import after_skie from '@site/static/samples/suspend/after_skie@2x.png';
import {checkMark} from "./Icons";
import {idea, xcode} from "./CodeBackgrounds";

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
    return <div className="py-16 border-0 border-t border-solid border-slate-100">
        <div className="mx-auto grid md:grid-cols-feature-right items-center gap-8">
            <div className="pt-2">
                <MacOSWindow tabs={tabs}/>
            </div>

            <div className="items-center px-4 sm:px-6">
                <h3 className="h3 mb-3">Suspend Interop</h3>
                <p className="text-xl text-gray-700 dark:text-gray-400 mb-4">Kotlin suspend functions are converted to Swift's native async functions.</p>
                <ul className="text-lg text-gray-700 dark:text-gray-400 -mb-2 p-0">
                    <li className="flex items-center mb-2">
                        {checkMark("lime")}
                        <span>Cancellation support</span>
                    </li>
                    <li className="flex items-center mb-2">
                        {checkMark("lime")}
                        <span>Callable from any thread</span>
                    </li>
                    <li className="flex items-center mb-2">
                        {checkMark("lime")}
                        <span>No wrapping necessary</span>
                    </li>
                </ul>
            </div>
        </div>
    </div>
}
