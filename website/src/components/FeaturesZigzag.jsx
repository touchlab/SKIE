import React, {useState} from 'react';
import {Android, AndroidOnly, Apple, AppleOnly, DecisionProcess, DecisionProcessOnly} from "./FeatureIcons";

import FeatImage01 from '@site/static/componentimg/features-03-image-01.png';
import FeatImage02 from '@site/static/componentimg/features-03-image-02.png';
import FeatImage03 from '@site/static/componentimg/features-03-image-03.png';

import enumKotlin from '@site/static/samples/enum/kotlin.png';
import enumSwiftBefore from '@site/static/samples/enum/swiftbefore.png';
import enumSwiftAfter from '@site/static/samples/enum/swiftafter.png';
import sealedKotlin from '@site/static/samples/sealed-class/kotlin.png';
import sealedSwiftBefore from '@site/static/samples/sealed-class/swiftbefore.png';
import sealedSwiftAfter from '@site/static/samples/sealed-class/swiftafter.png';
import sealedSwiftAfterComplete from '@site/static/samples/sealed-class/swiftaftercomplete.png';

function ShowKotlinSwiftToggle(kotlinBlock, swiftBlock){
    const [showKotlin, setShowKotlin] = useState(true);
    return (
        <div className=" flex flex-wrap justify-center -m-2 my-8">
          <div
              className={`flex flex-row justify-center items-center font-medium py-2 px-4 m-2 bg-gray-100 dark:bg-gray-800 rounded-full group transition duration-200 ${!showKotlin && 'opacity-50 hover:opacity-75 cursor-pointer'}`}
              onClick={() => { setShowKotlin(true);}}
          >
            <span className="mr-2 p-0 -mb-1">{AndroidOnly("lime")}</span>
            <span className="text-gray-600 group-hover:text-gray-800 dark:text-gray-400 dark:group-hover:text-gray-200 transition-colors duration-150 ease-in-out">Kotlin</span>
          </div>
          <div
              className={`flex flex-row justify-center items-center font-medium py-2 px-4 m-2 bg-gray-100 dark:bg-gray-800 rounded-full group transition duration-200 ${showKotlin && 'opacity-50 hover:opacity-75 cursor-pointer'}`}
              onClick={() => { setShowKotlin(false);}}
          >
            <span className="mr-2 p-0 -mb-1">{AppleOnly("lime")}</span>
            <span className="text-gray-600 group-hover:text-gray-800 dark:text-gray-400 dark:group-hover:text-gray-200 transition-colors duration-150 ease-in-out">Swift</span>
          </div>
            {showKotlin ? kotlinBlock() : swiftBlock()}
        </div>
    )
}

function labelImage(label, imgObj) {
    return (
        <>
            <img src={imgObj}
                 alt="Features 01"/><br/>
        </>
    )
}

function ToggleBlock(currentBlock, blockName, setBlock, title, svgBody){
    return(
        <div
            className={`flex flex-row justify-center items-center font-medium py-2 px-4 m-2 bg-gray-100 dark:bg-gray-800 rounded-full group ${currentBlock !== blockName && 'opacity-50 hover:opacity-75 cursor-pointer'}`}
            onClick={() => { setBlock(blockName);}}
        >
            <span className="mr-2 p-0 -mb-1">{svgBody()}</span>
            <span className="text-gray-600 group-hover:text-gray-800 dark:text-gray-400 dark:group-hover:text-gray-200 transition-colors duration-150 ease-in-out">{title}</span>
        </div>
    )
}

function ShowKotlinSwiftFriends(imgK, imgS, imgSAfter, imgSAfterComplete) {
    const [block, setBlock] = useState("kotlin");

  return (
      <>
      <div className="max-w-6xl mx-auto px-4 sm:px-6">
          <div className="flex flex-wrap justify-center -m-2 my-8">
              {ToggleBlock(
                  block,
                  "kotlin",
                  setBlock,
                  "Kotlin",
                  AndroidOnly
              )}
              {ToggleBlock(
                  block,
                  "noskie",
                  setBlock,
                  "Without SKIE",
                  AppleOnly
              )}
              {ToggleBlock(
                  block,
                  "withskie",
                  setBlock,
                  "With SKIE",
                  DecisionProcessOnly
              )}
              {imgSAfterComplete && ToggleBlock(
                  block,
                  "withskiecomplete",
                  setBlock,
                  "With SKIE Fixed",
                  DecisionProcessOnly
              )}

          </div>
      </div>
      <div className="relative max-w-4xl mx-auto text-center">
          <img className="absolute invisible" src={imgK}
               alt="Features 01"/>
          {/* Image */}
          {block === "kotlin" &&
              <>
            {labelImage("Kotlin", imgK)}
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
          {imgSAfterComplete && block === "withskiecomplete" &&
              <>
                  {labelImage("With SKIE ✅", imgSAfterComplete)}
              </>
          }

        </div>
      </>
  )
}

export default function FeaturesZigzag() {
    const checkMark = (color) => {
        return (
            <svg className={`w-3 h-3 fill-current text-${color}-500 mr-2 shrink-0" viewBox="0 0 12 12`} xmlns="http://www.w3.org/2000/svg">
                <path d="M10.28 2.28L3.989 8.575 1.695 6.28A1 1 0 00.28 7.695l3 3a1 1 0 001.414 0l7-7A1 1 0 0010.28 2.28z" />
            </svg>
        )
    }
    return (
    <section>
      <div className="hidden text-lime-600"></div>
      <div className="max-w-4xl mx-auto px-4 sm:px-6">
        <div className="pb-12 md:pb-20 border-t border-gray-800">

          {/*border-gray-800 border-solid border-t border-b-0 border-x-0*/}
          <div className="max-w-3xl mx-auto text-center">
            <h2 className="h2 mb-4">Kotlin to Swift Code Transformations</h2>
          </div>

          {/* Items */}
          <div className="">

            <div className="items-center">
              <div className="font-architects-daughter text-xl text-lime-700 dark:text-lime-300 mb-2">Exhaustive enums</div>
              <h3 className="h3 mb-3">Kotlin to Enum Transform</h3>
              <p className="text-xl text-gray-700 dark:text-gray-400 mb-4">Kotlin enums are automatically and transparently converted to Swift enums.</p>
              <ul className="text-lg text-gray-700 dark:text-gray-400 -mb-2">
                <li className="flex items-center mb-2">
                    {checkMark("green")}
                  <span>Proper compile-time checking</span>
                </li>
                <li className="flex items-center mb-2">
                    {checkMark("green")}
                  <span>Swift-native ergonomics</span>
                </li>
              </ul>
            </div>

            {/* 1st item */}

              {ShowKotlinSwiftFriends(enumKotlin, enumSwiftBefore, enumSwiftAfter)}

          </div>

        </div>
      </div>
        <div className="max-w-4xl mx-auto px-4 sm:px-6">
            <div className="pb-12 md:pb-20 border-t border-gray-800">

                {/* Items */}
                <div className="">

                    <div className="items-center">
                        <div className="font-architects-daughter text-xl text-lime-700 dark:text-lime-300 mb-2">Sealed class support</div>
                        <h3 className="h3 mb-3">Sealed class wrapped as an enum</h3>
                        <p className="text-xl text-gray-700 dark:text-gray-400 mb-4">Sealed classes are unchanged, but an associated enum is
                        generated, as well as a wrapper function to use for switch statements.</p>
                        <ul className="text-lg text-gray-700 dark:text-gray-400 -mb-2">
                            <li className="flex items-center mb-2">
                                {checkMark("green")}
                                <span>Sealed classes can be exhaustively checked</span>
                            </li>
                            <li className="flex items-center mb-2">
                                {checkMark("green")}
                                <span>Similar semantics to enums with associated values</span>
                            </li>
                        </ul>
                    </div>

                    {/* 1st item */}

                    {ShowKotlinSwiftFriends(sealedKotlin, sealedSwiftBefore, sealedSwiftAfter, sealedSwiftAfterComplete)}

                </div>

            </div>
        </div>
    </section>
  );
}
