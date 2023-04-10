import React from 'react';

export default function Subsection({id, contentClassName, padding, divider, children}) {
    const paddingOrDefault = padding || 'py-8 md:py-16 last:pb-0'
    const contentClassNameOrDefault = contentClassName || ''
    const dividerOrDefault = divider === undefined || divider ? <div className='subsection-divider '/> : <></>
    return (
        <>
            {dividerOrDefault}
            <div id={id} className={`${paddingOrDefault} ${contentClassNameOrDefault}`}>
                {children}
            </div>
        </>
    )
}
