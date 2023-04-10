import React, {useState} from 'react';
import {AndroidOnly, AppleOnly, DecisionProcessOnly} from "./FeatureIcons";
import CenteringSection from "./base/CenteringSection";
import SectionHeader from "./base/SectionHeader";

function ToggleBlock(currentBlock, blockName, setBlock, title, svgBody) {
    return (
        <div
            className={`flex flex-row justify-center items-center font-medium py-2 px-4 bg-slate-200 dark:bg-gray-800 rounded-full group transition duration-200 cursor-default space-x-2 relative ${currentBlock !== blockName && 'opacity-50 hover:opacity-75 !cursor-pointer'} ${currentBlock === blockName && "before:absolute before:content-[''] before:rounded-full before:-inset-1 before:border-2 before:border-solid before:border-slate-500"}`}
            onClick={() => {
                setBlock(blockName);
            }}
        >
            <span className="p-0 -mb-1">{svgBody()}</span>
            <span
                className={`text-gray-600 dark:text-gray-400 dark:group-hover:text-gray-200 transition-colors duration-150 ease-in-out ${currentBlock !== blockName && 'group-hover:text-gray-800 '}`}>{title}</span>
        </div>
    )
}

function DetailBlock(title, subTitle, descriptionBlock) {
    return (
        <div className="bg-gray-100 dark:bg-gray-800 px-6 md:px-10 lg:px-14 md:py-8 py-4">
            <h3 className="h4 md:h3 mb-3">{title}</h3>
            <div className="font-architects-daughter text-xl text-cyan-800 dark:text-cyan-300 mb-2">{subTitle}</div>
            <div className="md:text-xl text-lg text-gray-900 dark:text-gray-200 text-justify">
                {descriptionBlock()}
            </div>
        </div>
    )
}

export default function StakeholderValue() {
    const [block, setBlock] = useState("android");

    return (
        <CenteringSection background='bg-slate-50'>
            <SectionHeader title="Better APIs are valuable for everybody" />

            <div className="flex flex-wrap justify-center -m-2 my-8 gap-4">

                {ToggleBlock(
                    block,
                    "android",
                    setBlock,
                    "Android Devs",
                    AndroidOnly
                )}
                {ToggleBlock(
                    block,
                    "ios",
                    setBlock,
                    "iOS Devs",
                    AppleOnly
                )}
                {ToggleBlock(
                    block,
                    "managers",
                    setBlock,
                    "Engineering Managers",
                    DecisionProcessOnly
                )}
            </div>
            <div className='centering-section-inner-margins-reset'>

                {block === "android" &&
                    <>
                        {DetailBlock("Android Developers", 'No API Anxiety', () => {
                            return (<>
                                <p>
                                    Kotlin Multiplatform enables Android developers to write code that can be shared with their iOS colleagues. However, doing so results in a loss of many Kotlin language features even though both Kotlin and Swift support them. That makes it difficult for Android developers to judge how their code will look like from Swift.
                                </p>
                                <p>
                                    However, with SKIE, Kotlin developers don’t have to worry about these complexities of cross-platform development. Their Kotlin code will retain its modern and expressive features and will seamlessly integrate with Swift. That way, developers can focus on more meaningful and engaging tasks.
                                </p>
                            </>)
                        })}
                    </>
                }

                {block === "ios" &&
                    <>
                        {DetailBlock("iOS Developers", 'Swift-native API surface', () => {
                            return (<>
                                <p>
                                    Kotlin Multiplatform can be a powerful tool for mobile teams who want to share code across platforms. However, the API Kotlin exposes to Swift lacks many language features that both Kotlin and Swift support. As a result, iOS developers may be hesitant to adopt this technology.
                                </p>
                                <p>
                                    Fortunately, SKIE offers a solution that restores the expressiveness of modern languages, allowing developers to focus on their work without worrying about these limitations. This enables a smoother and more efficient development process, while also reducing maintenance costs and decreasing the likelihood of bugs.
                                </p>
                            </>)
                        })}
                    </>
                }

                {block === "managers" &&
                    <>
                        {DetailBlock("Engineering Managers", 'Better team collaboration', () => {
                            return (<>
                                <p>When adopting Kotlin Multiplatform, the Android team usually takes the lead. However, for a successful implementation of this cross-platform technology, the participation of all team members is essential. One potential challenge is that the API Kotlin exposes to Swift lacks many important language features. As a result, Kotlin Multiplatform may not resonate well with iOS developers, resulting in a hands-off approach.</p>
                                <p>To ensure that your team can leverage the full potential of Kotlin Multiplatform, it is therefore crucial to invest in a premium iOS developer experience. By doing so, you can empower your team to effectively collaborate and produce high-quality KMM-enabled apps.</p>
                            </>)
                        })}
                    </>
                }
            </div>
        </CenteringSection>
    )
}
