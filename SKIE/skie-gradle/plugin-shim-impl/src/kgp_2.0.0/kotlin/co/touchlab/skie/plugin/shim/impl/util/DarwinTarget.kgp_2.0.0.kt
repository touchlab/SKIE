package co.touchlab.skie.plugin.shim.impl.util

import co.touchlab.skie.plugin.shim.impl.util.DarwinTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

actual fun getAllSupportedDarwinTargets(): List<DarwinTarget> =
    listOf(
        DarwinTarget(KonanTarget.IOS_ARM64, "arm64-apple-ios", "iphoneos"),
        DarwinTarget(KonanTarget.IOS_X64, "x86_64-apple-ios-simulator", "iphonesimulator"),
        DarwinTarget(KonanTarget.IOS_SIMULATOR_ARM64, "arm64-apple-ios-simulator", "iphonesimulator"),
        DarwinTarget(KonanTarget.WATCHOS_ARM32, "armv7k-apple-watchos", "watchos"),
        DarwinTarget(KonanTarget.WATCHOS_ARM64, "arm64_32-apple-watchos", "watchos"),
        DarwinTarget(KonanTarget.WATCHOS_DEVICE_ARM64, "arm64-apple-watchos", "watchos"),
        DarwinTarget(KonanTarget.WATCHOS_X64, "x86_64-apple-watchos-simulator", "watchsimulator"),
        DarwinTarget(KonanTarget.WATCHOS_SIMULATOR_ARM64, "arm64-apple-watchos-simulator", "watchsimulator"),
        DarwinTarget(KonanTarget.TVOS_ARM64, "arm64-apple-tvos", "appletvos"),
        DarwinTarget(KonanTarget.TVOS_X64, "x86_64-apple-tvos-simulator", "appletvsimulator"),
        DarwinTarget(KonanTarget.TVOS_SIMULATOR_ARM64, "arm64-apple-tvos-simulator", "appletvsimulator"),
        DarwinTarget(KonanTarget.MACOS_X64, "x86_64-apple-macos", "macosx"),
        DarwinTarget(KonanTarget.MACOS_ARM64, "arm64-apple-macos", "macosx"),
    )
