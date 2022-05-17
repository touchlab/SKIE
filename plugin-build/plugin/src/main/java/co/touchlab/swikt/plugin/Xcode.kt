/*
 * Copyright 2010-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.touchlab.swikt.plugin

import org.codehaus.groovy.runtime.ProcessGroovyMethods
import org.jetbrains.kotlin.konan.file.File

interface Xcode {
    val toolchain: String
    val macosxSdk: String
    val iphoneosSdk: String
    val iphonesimulatorSdk: String
    val version: String
    val appletvosSdk: String
    val appletvsimulatorSdk: String
    val watchosSdk: String
    val watchsimulatorSdk: String
    // Xcode.app/Contents/Developer/usr
    val additionalTools: String

    /**
     * TODO: `toLowerCase` is deprecated and should be replaced with `lowercase`, but
     * this code used in buildSrc which depends on bootstrap version of stdlib, so right version
     * of this function isn't available, please replace warning suppression with right function
     * when compatible version of bootstrap will be available.
     */
    @Suppress("DEPRECATION")
    fun pathToPlatformSdk(platformName: String): String = when (platformName.toLowerCase()) {
        "macosx" -> macosxSdk
        "iphoneos" -> iphoneosSdk
        "iphonesimulator" -> iphonesimulatorSdk
        "appletvos" -> appletvosSdk
        "appletvsimulator" -> appletvsimulatorSdk
        "watchos" -> watchosSdk
        "watchsimulator" -> watchsimulatorSdk
        else -> error("Unknown Apple platform: $platformName")
    }

    companion object {
        val current: Xcode by lazy {
            CurrentXcode
        }
    }
}

private object CurrentXcode : Xcode {

    override val toolchain by lazy {
        val ldPath = xcrun("-f", "ld") // = $toolchain/usr/bin/ld
        File(ldPath).parentFile.parentFile.parentFile.absolutePath
    }

    override val additionalTools: String by lazy {
        val bitcodeBuildToolPath = xcrun("-f", "bitcode-build-tool")
        File(bitcodeBuildToolPath).parentFile.parentFile.absolutePath
    }

    override val macosxSdk by lazy { getSdkPath("macosx") }
    override val iphoneosSdk by lazy { getSdkPath("iphoneos") }
    override val iphonesimulatorSdk by lazy { getSdkPath("iphonesimulator") }
    override val appletvosSdk by lazy { getSdkPath("appletvos") }
    override val appletvsimulatorSdk by lazy { getSdkPath("appletvsimulator") }
    override val watchosSdk: String by lazy { getSdkPath("watchos") }
    override val watchsimulatorSdk: String by lazy { getSdkPath("watchsimulator") }


    override val version by lazy {
        xcrun("xcodebuild", "-version")
            .removePrefix("Xcode ")
    }

    private fun xcrun(vararg args: String): String {
        return ("/usr/bin/xcrun " + args.joinToString(" ")).let(ProcessGroovyMethods::execute).let(ProcessGroovyMethods::getText)
    }

    private fun getSdkPath(sdk: String) = xcrun("--sdk", sdk, "--show-sdk-path")
}
