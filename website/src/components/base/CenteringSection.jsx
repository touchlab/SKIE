import React from 'react';

export default function CenteringSection({id, outerPadding, innerMargin, background, divider, children}) {
    const outerPaddingOrDefault = outerPadding || 'py-8 md:py-16'
    const backgroundOrDefault = background || ''
    const innerMarginOrDefault = innerMargin || 'centering-section-inner-margins'
    const dividerOrDefault = divider ? 'section-divider' : ''
    return (
        <section id={id} className={`${outerPaddingOrDefault} ${backgroundOrDefault} ${dividerOrDefault}`}>
            <div className='flex justify-center'>
                <div className={`max-w-5xl ${innerMarginOrDefault}`}>
                    {children}
                </div>
            </div>
        </section>
    )
}
