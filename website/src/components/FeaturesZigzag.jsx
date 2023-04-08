import React, {useState} from 'react';
import {AndroidOnly, ThumbDownTab, ThumbUpTab} from "./FeatureIcons";

import FlowInterop from './features/FlowInterop';
import SuspendInterop from './features/SuspendInterop';
import SealedClasses from "./features/SealedClasses";

function labelImage(label, imgObj) {
    return (imgObj instanceof Array) ? labelImageScroller(label, imgObj) : labelImageScroller(label, [imgObj])
}

function labelImageScroller(label, imgObjs) {
    const [imgIndex, setImgIndex] = useState(0);
    const adjustIndex = (adj) => {
        let newIndex = imgIndex + adj
        if (newIndex < 0) {
            newIndex = imgObjs.length - 1
        }
        setImgIndex(newIndex % imgObjs.length)
    }

    const currentIndex = imgIndex % imgObjs.length
    const lastImage = currentIndex === imgObjs.length - 1
    return (
        <>
            <img src={imgObjs[currentIndex]} alt={label} onClick={() => adjustIndex(1)}/>
            {currentIndex !== 0 &&
                <div className="absolute bottom-8 left-4">
                    {leftArrow(() => adjustIndex(-1))}
                </div>
            }

            {!lastImage &&
                <div className="absolute bottom-8 right-4">
                    {rightArrow(() => adjustIndex(1))}
                </div>
            }
        </>
    )
}

function leftArrow(onClick) {
    return (
        <svg onClick={onClick} className="cursor-pointer" xmlns="http://www.w3.org/2000/svg" height="64" width="64" viewBox="0 0 64 64">
            <title>left arrow</title>
            <g className="nc-icon-wrapper">
                <path
                    d="M47.621,3.793,46.207,2.379a1,1,0,0,0-1.414,0L15.879,31.293a1,1,0,0,0,0,1.414L44.793,61.621a1,1,0,0,0,1.414,0l1.414-1.414a1,1,0,0,0,0-1.414L20.828,32,47.621,5.207A1,1,0,0,0,47.621,3.793Z"
                    className="fill-current text-white"></path>
            </g>
        </svg>
    )
}

function rightArrow(onClick) {
    return (
        <svg onClick={onClick} className="cursor-pointer" xmlns="http://www.w3.org/2000/svg" height="64" width="64" viewBox="0 0 64 64">
            <title>right arrow</title>
            <g className="nc-icon-wrapper">
                <path
                    d="M19.207,2.379a1,1,0,0,0-1.414,0L16.379,3.793a1,1,0,0,0,0,1.414L43.172,32,16.379,58.793a1,1,0,0,0,0,1.414l1.414,1.414a1,1,0,0,0,1.414,0L48.121,32.707a1,1,0,0,0,0-1.414Z"
                    className="fill-current text-white"></path>
            </g>
        </svg>
    )
}

function ToggleBlock(currentBlock, blockName, setBlock, title, svgBody) {
    return (
        <div
            className={`flex flex-row justify-center items-center font-medium py-2 px-4 m-2 bg-gray-100 dark:bg-gray-800 rounded-full group ${currentBlock !== blockName && 'opacity-50 hover:opacity-75 cursor-pointer'}`}
            onClick={() => {
                setBlock(blockName);
            }}
        >
            <span className="mr-2 p-0 -mb-1">{svgBody()}</span>
            <span
                className="text-gray-600 group-hover:text-gray-800 dark:text-gray-400 dark:group-hover:text-gray-200 transition-colors duration-150 ease-in-out">{title}</span>
        </div>
    )
}

function ShowKotlinSwiftFriends(imgK, imgS, imgSAfter) {
    const [block, setBlock] = useState("kotlin");

    return (
        <>
            <div className="relative max-w-4xl mx-auto text-center">
                <img className="absolute invisible top-0 left-0" src={imgK}
                     alt="Features 01"/>
                {/* Image */}
                {block === "kotlin" &&
                    <>
                        <MacOSWindow tabs={[
                            { title: "Kotlin", content: labelImage("Kotlin", imgK) }
                        ]}/>
                    </>
                }
                {block === "noskie" &&
                    <>
                        {labelImage("Without SKIE ❌", imgS)}
                    </>
                }
                {block === "withskie" &&
                    <>
                        {labelImage("With SKIE ✅", imgSAfter)}
                    </>
                }

            </div>
            <div className="max-w-6xl mx-auto px-4 sm:px-6">
                <div className="flex flex-wrap justify-center -m-2 my-2">
                    {ToggleBlock(
                        block,
                        "kotlin",
                        setBlock,
                        "Kotlin",
                        () => AndroidOnly("lime")
                    )}
                    {ToggleBlock(
                        block,
                        "noskie",
                        setBlock,
                        "Without SKIE",
                        ThumbDownTab
                    )}
                    {ToggleBlock(
                        block,
                        "withskie",
                        setBlock,
                        "With SKIE",
                        ThumbUpTab
                    )}

                </div>
            </div>
        </>
    )
}

export default function FeaturesZigzag() {
    const checkMark = (color) => {
        return (
            <svg className={`w-3 h-3 fill-current text-${color}-500 mr-2 shrink-0" viewBox="0 0 12 12`} xmlns="http://www.w3.org/2000/svg">
                <path d="M10.28 2.28L3.989 8.575 1.695 6.28A1 1 0 00.28 7.695l3 3a1 1 0 001.414 0l7-7A1 1 0 0010.28 2.28z"/>
            </svg>
        )
    }
    return (
        <section id="features" className="pt-8 md:pt-16">
            <div className="hidden text-lime-600"></div>
            <div className="hidden text-lime-500"></div>
            <div className="max-w-5xl mx-auto">
                <div className="border-t border-gray-800">
                    {/*border-gray-800 border-solid border-t border-b-0 border-x-0*/}
                    <div className="max-w-3xl mx-auto text-center">
                        <h2 className="h2 mb-4">Kotlin to Swift Code Transformations</h2>
                        <p className="text-xl text-gray-700 dark:text-gray-400 mb-4">
                            SKIE improves interoperability between Kotlin and Swift by generating Swift wrappers for Objective-C headers created by the Kotlin compiler. It recreates features supported by both languages but lost in the translation from Kotlin to Objective-C to Swift.
                        </p>
                    </div>

                    {/* Items */}
                    <FlowInterop/>
                    <SuspendInterop/>
                    <SealedClasses/>
                </div>
            </div>

        </section>
    );
}
