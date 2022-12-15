import React, {useState} from 'react';
import {AndroidOnly, AppleOnly, DecisionProcessOnly} from "./FeatureIcons";

function ToggleBlock(currentBlock, blockName, setBlock, title, svgBody){
  return(
      <div
          className={`flex flex-row justify-center items-center font-medium py-2 px-4 m-2 bg-gray-100 dark:bg-gray-800 rounded-full group transition duration-200 ${currentBlock !== blockName && 'opacity-50 hover:opacity-75 cursor-pointer'}`}
          onClick={() => { setBlock(blockName);}}
      >
        <span className="mr-2 p-0 -mb-1">{svgBody()}</span>
        <span className="text-gray-600 group-hover:text-gray-800 dark:text-gray-400 dark:group-hover:text-gray-200 transition-colors duration-150 ease-in-out">{title}</span>
      </div>
  )
}

function DetailBlock(title, subTitle, descriptionBlock){
  return (<div className="max-w-5xl mx-auto">
    <div className="bg-gray-100 dark:bg-gray-800 px-4 md:px-12 md:pt-8 pt-4 md:pb-4 pb-2">
      <h3 className="h4 md:h3 mb-3">{title}</h3>
      <div className="font-architects-daughter text-xl text-cyan-800 dark:text-cyan-300 mb-2">{subTitle}</div>
      <p className="md:text-xl text-lg text-gray-900 dark:text-gray-200">
        {descriptionBlock()}
      </p>
    </div>
  </div>)
}

export default function StakeholderValue() {
  const [block, setBlock] = useState("android");

  return (
    <section>
      <div className="max-w-6xl mx-auto px-4 sm:px-6">
        <div className="border-t border-gray-800">
          {/* Section header */}
          <div className="max-w-3xl mx-auto text-center">
            <h2 className="h2 mb-4">Better APIs are valuable for everybody.</h2>
          </div>
        </div>
        <div className="flex flex-wrap justify-center -m-2 my-8">

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
        <div className="pb-10 md:pb-16 border-t border-gray-800">

          {block === "android" &&
              <>
                {DetailBlock("Android Developers", 'No API Anxiety', () => {
                  return (<>Most Kotlin developers have little experience with Swift, and don't know what
                    the KMP API surface will look like when called from Swift. SKIE significantly reduces these issues.
                    Kotlin code can be written with
                    expressive, modern features, and developers can be confident that when called from Swift, the
                    interface will feel native.</>)
                })}
              </>
          }

          {block === "ios" &&
              <>
                {DetailBlock("iOS Developers", 'Swift-native API surface', () => {
                  return (<>Most iOS teams have spent the past few years adopting Swift at scale.
                    While Swift interops with Objective-C fairly well, pushing Kotlin through an Objective-C lens strips
                    most modern language
                    features that both Kotlin and Swift support. The stock API surface from KMM is not just unpleasant:
                    these modern language
                    features exist to make development safer and more productive.</>)
                })}
              </>
          }

          {block === "managers" &&
              <>
                {DetailBlock("Engineering Managers", 'Better team collaboration', () => {
                  return (<>Sharing code, when done well, results in better development efficiency, lower maintenance
                  costs, less potential for bugs, and of course, more consistent feature parity. When teams start with KMM,
                  the development tends to come from the Android side, because the language and tooling is familiar. Over time,
                  encouraging "iOS" and "Android" developers to become a team of "Mobile" developers improves all of these metrics.
                  An Objective-C interface, with "lowest common denominator" language features, doesn't often send a positive
                  message to the iOS team members, and that often results in a hands-off approach. Investing in a premium iOS
                  developer experience will have significant ROI.</>)
                })}
              </>
          }
        </div>
      </div>
    </section>
  );
}
