import React from "react";

export default function SectionHeader({title, textColor, includeMargin, children}) {
    let childrenContent = children && <p className="text-xl text-gray-700 dark:text-gray-400">
        {children}
    </p>
    const bottomMargin = includeMargin === undefined || includeMargin ? 'mb-8 md:mb-10' : ''
    const textColorOrDefault = textColor ?? ''

    return (
        <div className={`max-w-3xl mx-auto text-center ${bottomMargin} ${textColorOrDefault}`}>
            <h1 className="h2 mb-4">{title}</h1>
            {childrenContent}
        </div>
    )
}
