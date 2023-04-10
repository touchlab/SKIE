import React from 'react';

export default function CenteringSection({id, outerPadding, innerMargin, background, divider, children}) {
    const outerPaddingOrDefault = outerPadding || 'py-8 md:py-16'
    const backgroundOrDefault = background || ''
    const innerMarginOrDefault = innerMargin || 'centering-section-inner-margins'

    return <>
        {divider && <div className={backgroundOrDefault}><div className={`max-w-5xl grow section-divider mx-auto`}/></div>}
        <section id={id} className={`${outerPaddingOrDefault} ${backgroundOrDefault}`}>
            <div className='flex justify-center'>
                <div className={`max-w-5xl grow ${innerMarginOrDefault}`}>
                    {children}
                </div>
            </div>
        </section>
    </>
}
