import React, {useState} from 'react';
import {Android, AndroidOnly, Apple, AppleOnly, DecisionProcess, DecisionProcessOnly} from "./FeatureIcons";

import FeatImage01 from '@site/static/componentimg/features-03-image-01.png';
import FeatImage02 from '@site/static/componentimg/features-03-image-02.png';
import FeatImage03 from '@site/static/componentimg/features-03-image-03.png';

import enumKotlin from '@site/static/samples/enum/kotlin.png';
import enumSwiftBefore from '@site/static/samples/enum/swiftbefore.png';
import enumSwiftAfter from '@site/static/samples/enum/swiftafter.png';
import sealedKotlin from '@site/static/samples/sealed-class/kotlin-sealed-class.png';
import sealedSwiftBefore from '@site/static/samples/sealed-class/swift-before-skie-sealed-class.png';
import sealedSwiftAfter from '@site/static/samples/sealed-class/swift-after-skie-sealed-class.png';

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
            <div
                className="w-48 mt-4 mb-2 text-center inline-block text-black bg-slate-400 dark:text-white dark:bg-slate-600 py-1 px-3 rounded-full text-lg">{label}</div>
            <br/>
            <img className="h-auto w-[580px]" src={imgObj}
                 alt="Features 01"/><br/>
        </>
    )
}

function ShowKotlinSwiftFriends(imgK, imgS, imgSAfter) {
  return (
      <>
        {/*<div className="block md:hidden items-center">
          {ShowKotlinSwiftToggle(() => {
            return (
                <img className="max-w-full mx-auto md:max-w-none h-auto" src={imgK} width="540" height="auto"
                     alt="Features 01"/>
            )
          }, () => {
            return (
                <img className="max-w-full mx-auto md:max-w-none h-auto" src={imgS} width="540" height="auto"
                     alt="Features 01"/>
            )
          })}
        </div>*/}

      <div className="max-w-6xl mx-auto text-center">
          {/* Image */}
            {labelImage("Kotlin", imgK)}
            {labelImage("Without SKIE ❌", imgS)}
            {labelImage("With SKIE ✅", imgSAfter)}
        </div>
      </>
  )
}

function FeaturesZigzag() {

    return (
    <section>
      <div className="hidden text-lime-600"></div>
      <div className="max-w-6xl mx-auto px-4 sm:px-6">
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
                  <svg className="w-3 h-3 fill-current text-green-500 mr-2 shrink-0" viewBox="0 0 12 12" xmlns="http://www.w3.org/2000/svg">
                    <path d="M10.28 2.28L3.989 8.575 1.695 6.28A1 1 0 00.28 7.695l3 3a1 1 0 001.414 0l7-7A1 1 0 0010.28 2.28z" />
                  </svg>
                  <span>Proper compile-time checking</span>
                </li>
                <li className="flex items-center mb-2">
                  <svg className="w-3 h-3 fill-current text-green-500 mr-2 shrink-0" viewBox="0 0 12 12" xmlns="http://www.w3.org/2000/svg">
                    <path d="M10.28 2.28L3.989 8.575 1.695 6.28A1 1 0 00.28 7.695l3 3a1 1 0 001.414 0l7-7A1 1 0 0010.28 2.28z" />
                  </svg>
                  <span>Swift-native ergonomics</span>
                </li>
              </ul>
            </div>

            {/* 1st item */}

              {ShowKotlinSwiftFriends(enumKotlin, enumSwiftBefore, enumSwiftAfter)}

          </div>

        </div>
      </div>
    </section>
  );
}

export default FeaturesZigzag;
