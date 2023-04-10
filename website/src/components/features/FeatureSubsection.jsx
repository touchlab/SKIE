import Subsection from "../base/Subsection";
import React from "react";

function CheckMark() {
    return (
        <svg className={`w-3 h-3 fill-current text-lime-500 mr-2 shrink-0" viewBox="0 0 12 12`} xmlns="http://www.w3.org/2000/svg">
            <path d="M10.28 2.28L3.989 8.575 1.695 6.28A1 1 0 00.28 7.695l3 3a1 1 0 001.414 0l7-7A1 1 0 0010.28 2.28z"/>
        </svg>
    )
}

export default function FeatureSubsection({title, description, benefits, contentLeft, isFirst, children}) {
    const titlePosition = contentLeft ? 'lg:order-last' : 'lg:order-first'
    const contentPosition = contentLeft ? 'lg:order-first' : 'lg:order-last'
    const gridColumns = contentLeft ? 'lg:grid-cols-[5fr_3fr]' : 'lg:grid-cols-[3fr_5fr]'

    return (
        <div className={`mx-auto grid gap-8 items-center ${gridColumns}`}>
            <div className={`max-sm:mx-6 items-center ${titlePosition}`}>
                <h3 className="h3 mb-3">{title}</h3>
                <p className="text-xl text-gray-700 dark:text-gray-400 mb-4">{description}</p>
                <ul className="text-lg text-gray-700 dark:text-gray-400 -mb-2 p-0">
                    {benefits.map((benefit, index) => (
                        <li key={index} className="flex items-center mb-2">
                            <CheckMark/>
                            <span>{benefit}</span>
                        </li>
                    ))}
                </ul>
            </div>

            <div className={contentPosition}>{children}</div>
        </div>
    )
}
