import React from 'react';

export function Circle({colors, children}) {
    return (
        <svg className="w-16 h-16 mb-4 origin-center" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
            <rect className={`${colors.background}`} width="64" height="64" rx="32"/>
            <g className={`${colors.primary} origin-center`} strokeLinecap="square" strokeLinejoin="miter" strokeWidth="2" fill="none" strokeMiterlimit="10">
            {children}
            </g>
        </svg>
    )
}


function CircleBlock(svgBody) {
    return (
        <svg className="w-16 h-16 mb-4" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
            <rect className="fill-current text-amber-300 dark:text-amber-400" width="64" height="64" rx="32"/>
            {svgBody()}
        </svg>
    )
}

function WhiteCircleBlock(svgBody) {
    return (
        <svg className="w-16 h-16" viewBox="0 0 64 64" xmlns="http://www.w3.org/2000/svg">
            <rect className="fill-current text-gray-100" width="64" height="64" rx="32"/>
            {svgBody()}
        </svg>
    )
}

export function FavList() {
    return (
        <>{CircleBlock(() => {
            return (
                <>
                    <path className="stroke-current text-amber-100"
                          d="M30 39.313l-4.18 2.197L27 34.628l-5-4.874 6.91-1.004L32 22.49l3.09 6.26L42 29.754l-3 2.924"
                          strokeLinecap="square" strokeWidth="2" fill="none" fillRule="evenodd"/>
                    <path className="stroke-current text-amber-300" d="M43 42h-9M43 37h-9" strokeLinecap="square"
                          strokeWidth="2"/>
                </>
            )
        })}</>
    )
}

export function Note() {
    return (
        <>{CircleBlock(() => {
            return (
                <>
                    <path className="stroke-current text-amber-100" strokeWidth="2" strokeLinecap="square" d="M21 23h22v18H21z" fill="none"
                          fillRule="evenodd"/>
                    <path className="stroke-current text-amber-300" d="M26 28h12M26 32h12M26 36h5" strokeWidth="2" strokeLinecap="square"/>
                </>
            )
        })}</>
    )
}

export function World2() {
    return (
        <>{CircleBlock(() => {
            return (
                <g transform="translate(21 21)" strokeLinecap="square" strokeWidth="2" fill="none" fillRule="evenodd">
                    <ellipse className="stroke-current text-amber-300" cx="11" cy="11" rx="5.5" ry="11"/>
                    <path className="stroke-current text-amber-100" d="M11 0v22M0 11h22"/>
                    <circle className="stroke-current text-amber-100" cx="11" cy="11" r="11"/>
                </g>
            )
        })}</>
    )
}

export function Cyborg() {
    return (
        <>{CircleBlock(() => {
            return (
                <g transform="translate(22 21)" strokeLinecap="square" strokeWidth="2" fill="none" fillRule="evenodd">
                    <path className="stroke-current text-amber-100"
                          d="M17 22v-6.3a8.97 8.97 0 003-6.569A9.1 9.1 0 0011.262 0 9 9 0 002 9v1l-2 5 2 1v4a2 2 0 002 2h4a5 5 0 005-5v-5"/>
                    <circle className="stroke-current text-amber-300" cx="13" cy="9" r="3"/>
                </g>
            )
        })}</>
    )
}

export function ThumbUp() {
    return (
        <>{CircleBlock(() => {
            return (
                <g strokeLinecap="square" strokeWidth="2" fill="none" fillRule="evenodd">
                    <path className="stroke-current text-amber-100"
                          d="M29 42h10.229a2 2 0 001.912-1.412l2.769-9A2 2 0 0042 29h-7v-4c0-2.373-1.251-3.494-2.764-3.86a1.006 1.006 0 00-1.236.979V26l-5 6"/>
                    <path className="stroke-current text-amber-300" d="M22 30h4v12h-4z"/>
                </g>
            )
        })}</>
    )
}

export function Messaging() {
    return (
        <>{CircleBlock(() => {
            return (
                <g transform="translate(21 22)" strokeLinecap="square" strokeWidth="2" fill="none" fillRule="evenodd">
                    <path className="stroke-current text-amber-300"
                          d="M17 2V0M19.121 2.879l1.415-1.415M20 5h2M19.121 7.121l1.415 1.415M17 8v2M14.879 7.121l-1.415 1.415M14 5h-2M14.879 2.879l-1.415-1.415"/>
                    <circle className="stroke-current text-amber-300" cx="17" cy="5" r="3"/>
                    <path className="stroke-current text-amber-100"
                          d="M8.86 1.18C3.8 1.988 0 5.6 0 10c0 5 4.9 9 11 9a10.55 10.55 0 003.1-.4L20 21l-.6-5.2a9.125 9.125 0 001.991-2.948"/>
                </g>
            )
        })}</>
    )
}

export function NetworkConnection() {
    return (
        <>{CircleBlock(() => {
            return (
                <g transform="translate(19 20)" strokeLinecap="square" strokeWidth="2" fill="none" strokeLinejoin="miter"
                   className="nc-icon-wrapper" strokeMiterlimit="10">
                    <line data-cap="butt" x1="8.6" y1="10.2" x2="15.4" y2="6.8" strokeLinecap="butt"
                          className="stroke-current text-amber-600"></line>
                    <line data-cap="butt" x1="8.6" y1="13.7" x2="15.4" y2="17.1" strokeLinecap="butt"
                          className="stroke-current text-amber-600"></line>
                    <circle cx="5" cy="12" r="4" className="stroke-current text-amber-900"></circle>
                    <circle cx="19" cy="5" r="4" className="stroke-current text-amber-900"></circle>
                    <circle cx="19" cy="19" r="4" className="stroke-current text-amber-900"></circle>
                </g>
            )
        })}</>
    )
}

export function Geometry() {
    return (
        <>{CircleBlock(() => {
            return (
                <g strokeLinecap="square" strokeWidth="2" fill="none" strokeLinejoin="miter"
                   className="nc-icon-wrapper" strokeMiterlimit="10">
                    <g transform="translate(20 20)">
                        <circle cx="18.5" cy="8.5" r="4.5" className="stroke-current text-amber-600"></circle>
                        <rect x="6" y="15" width="7" height="7" className="stroke-current text-amber-600"></rect>
                        <polygon points="2 10 6 3 10 10 2 10" className="stroke-current text-amber-900"></polygon>
                    </g>
                </g>
            )
        })}</>
    )
}

export function PillBottle() {
    return (
        <>{CircleBlock(() => {
            return (
                <g strokeLinecap="square" strokeWidth="2" fill="none" strokeLinejoin="miter"
                   className="nc-icon-wrapper" strokeMiterlimit="10">
                    <g transform="translate(19 20)">
                        <rect x="1" y="1" width="22" height="5" className="stroke-current text-amber-600"></rect>
                        <polyline points="21,6 21,23 3,23 3,6 " className="stroke-current text-amber-900"></polyline>
                        <rect x="7" y="11" width="10" height="6" className="stroke-current text-amber-600"></rect>
                        <line x1="6" y1="6" x2="6" y2="4" className="stroke-current text-amber-600"></line>
                        <line x1="10" y1="6" x2="10" y2="4" className="stroke-current text-amber-600"></line>
                        <line x1="14" y1="6" x2="14" y2="4" className="stroke-current text-amber-600"></line>
                        <line x1="18" y1="6" x2="18" y2="4" className="stroke-current text-amber-600"></line>
                    </g>
                </g>
            )
        })}</>
    )
}

export function Link() {
    return (
        <>{CircleBlock(() => {
            return (
                <g strokeLinecap="square" strokeWidth="2" fill="none" strokeLinejoin="miter"
                   className="nc-icon-wrapper" strokeMiterlimit="10">
                    <g transform="translate(20 20)">
                        <path
                            d="M13.4,10.6 L13.4,10.6c2,2,2,5.1,0,7.1l-2.8,2.8c-2,2-5.1,2-7.1,0l0,0c-2-2-2-5.1,0-7.1L6,11"
                            className="stroke-current text-amber-900"></path>
                        <path
                            d="M10.6,13.4L10.6,13.4 c-2-2-2-5.1,0-7.1l2.8-2.8c2-2,5.1-2,7.1,0l0,0c2,2,2,5.1,0,7.1L18,13"
                            className="stroke-current text-amber-600"></path>
                    </g>
                </g>
            )
        })}</>
    )
}

export function FilterOrganization() {
    return (
        <>{CircleBlock(() => {
            return (

                <g strokeLinecap="square" strokeWidth="2" fill="none" strokeLinejoin="miter"
                   className="nc-icon-wrapper" strokeMiterlimit="10">
                    <g transform="translate(20 20)">
                        <line x1="3" y1="13" x2="10" y2="13" className="stroke-current text-amber-100"></line>
                        <line x1="3" y1="5" x2="10" y2="5" className="stroke-current text-amber-100"></line>
                        <polyline points=" 3,1 3,21 10,21 " className="stroke-current text-amber-100"></polyline>
                        <rect x="10" y="3" width="11" height="4" className="stroke-current text-amber-300"></rect>
                        <rect x="10" y="19" width="11" height="4" className="stroke-current text-amber-300"></rect>
                        <rect x="10" y="11" width="11" height="4" className="stroke-current text-amber-300"></rect>
                    </g>
                </g>

            )
        })}</>
    )
}

const big = false

export function Apple() {
    return (
        <>{WhiteCircleBlock(() => {
            if (big) {
                return (
                    <g strokeLinecap="square" strokeWidth="2" fill="none" strokeLinejoin="miter"
                       className="nc-icon-wrapper" strokeMiterlimit="10">
                        <g transform="translate(16 13)">
                            <path className="stroke-current text-amber-100"
                                  d="M28,23.697 c-0.656,1.427-0.969,2.064-1.815,3.325c-1.177,1.761-2.839,3.954-4.897,3.973c-1.829,0.017-2.299-1.168-4.781-1.155 s-3,1.176-4.829,1.159c-2.058-0.018-3.631-1.999-4.81-3.761c-3.295-4.925-3.639-10.704-1.607-13.776 c1.444-2.184,3.723-3.461,5.865-3.461c2.181,0,3.552,1.174,5.356,1.174c1.75,0,2.815-1.176,5.338-1.176 c1.906,0,3.926,1.019,5.365,2.78C22.47,15.318,23.236,21.929,28,23.697L28,23.697z"></path>
                            <path className="stroke-current text-amber-300"
                                  d="M20.56,5.722 C21.515,4.497,22.239,2.768,21.976,1c-1.56,0.107-3.383,1.099-4.448,2.392c-0.966,1.173-1.765,2.914-1.455,4.606 C17.775,8.051,19.537,7.035,20.56,5.722L20.56,5.722z"
                            ></path>
                        </g>
                    </g>

                )
            } else {
                return (
                    <g strokeLinecap="square" strokeWidth="2" fill="none" strokeLinejoin="miter"
                       className="nc-icon-wrapper" strokeMiterlimit="10">
                        <g transform="translate(20 18)">
                            <path className="stroke-current text-gray-700"
                                  d="M21,17.423 c-0.492,1.09-0.727,1.576-1.361,2.54c-0.883,1.345-2.129,3.02-3.673,3.034c-1.372,0.013-1.724-0.892-3.586-0.882 c-1.861,0.01-2.25,0.898-3.622,0.885c-1.544-0.014-2.723-1.526-3.608-2.872C2.68,16.366,2.422,11.952,3.946,9.606 c1.083-1.668,2.792-2.643,4.399-2.643c1.636,0,2.664,0.897,4.017,0.897c1.312,0,2.112-0.898,4.003-0.898 c1.43,0,2.944,0.778,4.024,2.123C16.853,11.023,17.427,16.072,21,17.423L21,17.423z"></path>
                            <path
                                d="M15.1,3.45c0.65-0.834,1.143-2.011,0.964-3.214 c-1.062,0.073-2.302,0.748-3.027,1.628c-0.658,0.799-1.201,1.983-0.99,3.135C13.205,5.035,14.404,4.343,15.1,3.45L15.1,3.45z"
                                strokeLinejoin="miter" stroke="none" className="stroke-current text-cyan-600"></path>
                        </g>
                    </g>
                )
            }
        })}</>
    )
}

export function Android() {
    return (
        <>{WhiteCircleBlock(() => {
            if (big) {
                return (
                    <g strokeLinecap="square" strokeWidth="2" fill="none" strokeLinejoin="miter"
                       className="nc-icon-wrapper" strokeMiterlimit="10">
                        <g transform="translate(16 15)">
                            <path d="M31,23A15.154,15.154,0,0,0,1,23Z" className="stroke-current text-gray-700"></path>
                            <line x1="6" y1="6" x2="8.88" y2="11.76" className="stroke-current text-cyan-600"></line>
                            <circle cx="8.5" cy="18.5" r="1.5" stroke="none" className="stroke-current text-cyan-600"></circle>
                            <line x1="26" y1="6" x2="23.12" y2="11.76" className="stroke-current text-cyan-600"></line>
                            <circle cx="23.5" cy="18.5" r="1.5" stroke="none" className="stroke-current text-amber-300"></circle>
                        </g>
                    </g>
                )
            } else {
                return (
                    <g strokeLinecap="square" strokeWidth="2" fill="none" strokeLinejoin="miter"
                       className="nc-icon-wrapper" strokeMiterlimit="10">
                        <g transform="translate(20 19)">
                            <path d="M22.95,17a11,11,0,0,0-21.9,0Z" className="stroke-current text-gray-700"></path>
                            <line x1="5" y1="4" x2="7.404" y2="8.006" className="stroke-current text-cyan-600"></line>
                            <circle cx="7" cy="13" r="1" stroke="none" className="stroke-current text-cyan-600"></circle>
                            <line x1="19" y1="4" x2="16.596" y2="8.006" className="stroke-current text-cyan-600"></line>
                            <circle cx="17" cy="13" r="1" stroke="none" className="stroke-current text-cyan-600"></circle>
                        </g>
                    </g>
                )
            }

        })}</>
    )
}

export function DecisionProcess() {
    return (
        <>{WhiteCircleBlock(() => {
            return (
                <g strokeLinecap="square" strokeWidth="2" fill="none" strokeLinejoin="miter"
                   className="nc-icon-wrapper" strokeMiterlimit="10">
                    <g transform="translate(22 20)">
                        <rect x="2" y="2" width="8" height="4" className="stroke-current text-cyan-600"></rect>
                        <rect x="2" y="18" width="8" height="4" className="stroke-current text-cyan-600"></rect>
                        <line x1="6" y1="9" x2="6" y2="15" className="stroke-current text-gray-700"></line>
                        <polyline points="13 4 17 4 17 6" className="stroke-current text-gray-700"></polyline>
                        <polyline points="13 20 17 20 17 18" className="stroke-current text-gray-700"></polyline>
                        <polygon points="12 12 17 9 22 12 17 15 12 12"
                                 className="stroke-current text-cyan-600"></polygon>
                    </g>
                </g>
            )
        })}</>
    )
}

export function AppleOnly(color) {
    return (
        <svg xmlns="http://www.w3.org/2000/svg" height="28" width="24" viewBox="0 0 24 28"
             className={`fill-current text-${color ? color : "cyan"}-600`}><title>apple</title>
            <g transform="translate(0 2)" className="nc-icon-wrapper">
                <path
                    d="M21.354,16.487c-1.338-0.506-2.233-1.721-2.334-3.17c-0.099-1.412,0.593-2.666,1.851-3.355l1.046-0.573 l-0.747-0.93c-1.255-1.563-3.051-2.497-4.804-2.497c-1.215,0-2.058,0.318-2.735,0.574c-0.478,0.181-0.855,0.323-1.269,0.323 c-0.472,0-0.938-0.166-1.478-0.358c-0.708-0.252-1.51-0.538-2.54-0.538c-1.99,0-3.997,1.188-5.237,3.098 c-1.851,2.849-1.343,7.734,1.208,11.616C5.326,22.215,6.743,23.982,8.75,24c0.013,0,0.026,0,0.039,0 c1.643,0,2.003-0.876,3.598-0.886c1.742,0.082,1.962,0.893,3.589,0.882c1.961-0.018,3.375-1.771,4.499-3.484 c0.664-1.007,0.921-1.534,1.438-2.678l0.438-0.97L21.354,16.487z"></path>
                <path data-color="color-2"
                      d="M15.1,3.45c0.65-0.834,1.143-2.011,0.964-3.214c-1.062,0.073-2.302,0.748-3.027,1.628 c-0.658,0.799-1.201,1.983-0.99,3.135C13.205,5.035,14.404,4.343,15.1,3.45L15.1,3.45z"></path>
            </g>
        </svg>
    )
}

export function AndroidOnly(color, saturation) {
    return (
        <svg width="24" height="28" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 28"
             className={`fill-current text-${color ? color : "cyan"}-${saturation ? saturation : "600"}`}><title>android</title>
            <g transform="translate(0 4)" className="nc-icon-wrapper">
                <path
                    d="M17.6 9.48l1.84-3.18c.16-.31.04-.69-.26-.85a.637.637 0 0 0-.83.22l-1.88 3.24a11.463 11.463 0 0 0-8.94 0L5.65 5.67a.643.643 0 0 0-.87-.2c-.28.18-.37.54-.22.83L6.4 9.48A10.78 10.78 0 0 0 1 18h22a10.78 10.78 0 0 0-5.4-8.52zM7 15.25a1.25 1.25 0 1 1 0-2.5 1.25 1.25 0 0 1 0 2.5zm10 0a1.25 1.25 0 1 1 0-2.5 1.25 1.25 0 0 1 0 2.5z"
                ></path>
            </g>
        </svg>
    )
}

export function DecisionProcessOnly() {
    return (<svg xmlns="http://www.w3.org/2000/svg" height="28" width="24" viewBox="0 0 24 28"
                 className="fill-current text-cyan-600"><title>decision process</title>
        <g transform="translate(0 4)" className="nc-icon-wrapper">
            <path d="M10,7H2A1,1,0,0,1,1,6V2A1,1,0,0,1,2,1h8a1,1,0,0,1,1,1V6A1,1,0,0,1,10,7Z"></path>
            <path d="M10,23H2a1,1,0,0,1-1-1V18a1,1,0,0,1,1-1h8a1,1,0,0,1,1,1v4A1,1,0,0,1,10,23Z"></path>
            <rect x="5" y="8" width="2" height="8"></rect>
            <path d="M19,7H17V5H12V3h6a1,1,0,0,1,1,1Z"></path>
            <path d="M18,21H12V19h5V17h2v3A1,1,0,0,1,18,21Z"></path>
            <path
                d="M18,16a1,1,0,0,1-.515-.143l-5-3a1,1,0,0,1,0-1.714l5-3a1,1,0,0,1,1.03,0l5,3a1,1,0,0,1,0,1.714l-5,3A1,1,0,0,1,18,16Z"
                data-color="color-2"></path>
        </g>
    </svg>)
}

export function ThumbUpTab() {
    return (<svg xmlns="http://www.w3.org/2000/svg" height="18" width="18" viewBox="0 0 24 24" className="fill-current text-lime-600">
        <title>thumb up</title>
        <g className="nc-icon-wrapper">
            <path data-color="color-2" d="M4,11H1a1,1,0,0,0-1,1V23a1,1,0,0,0,1,1H4Z"></path>
            <path
                d="M22.539,10.4A4,4,0,0,0,19.5,9H13V4c0-2.206-.794-4-3-4a1,1,0,0,0-.965.737L6,11V24H18.426a3.979,3.979,0,0,0,3.954-3.392l1.076-7A4,4,0,0,0,22.539,10.4Z"
            ></path>
        </g>
    </svg>)
}

export function ThumbDownTab() {
    return (<svg xmlns="http://www.w3.org/2000/svg" height="18" width="18" viewBox="0 0 24 24" className="fill-current text-lime-600">
        <title>thumb down</title>
        <g className="nc-icon-wrapper">
            <path data-color="color-2" d="M4,13H1a1,1,0,0,1-1-1V1A1,1,0,0,1,1,0H4Z"></path>
            <path
                d="M22.539,13.605A4,4,0,0,1,19.5,15H13v5c0,2.206-.794,4-3,4a1,1,0,0,1-.965-.737L6,13V0H18.426A3.979,3.979,0,0,1,22.38,3.392l1.076,7A4,4,0,0,1,22.539,13.605Z"
            ></path>
        </g>
    </svg>)
}
