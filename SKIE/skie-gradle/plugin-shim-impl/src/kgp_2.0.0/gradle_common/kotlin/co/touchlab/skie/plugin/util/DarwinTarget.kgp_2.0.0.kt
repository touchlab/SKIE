package co.touchlab.skie.plugin.util

import org.jetbrains.kotlin.konan.target.KonanTarget

internal actual fun getAllSupportedDarwinTargets(): List<DarwinTarget> =
    listOf(
        DarwinTarget(KonanTarget.IOS_ARM64.name, "arm64-apple-ios", "iphoneos"),
        DarwinTarget(KonanTarget.IOS_X64.name, "x86_64-apple-ios-simulator", "iphonesimulator"),
        DarwinTarget(KonanTarget.IOS_SIMULATOR_ARM64.name, "arm64-apple-ios-simulator", "iphonesimulator"),
        DarwinTarget(KonanTarget.WATCHOS_ARM32.name, "armv7k-apple-watchos", "watchos"),
        DarwinTarget(KonanTarget.WATCHOS_ARM64.name, "arm64_32-apple-watchos", "watchos"),
        DarwinTarget(KonanTarget.WATCHOS_DEVICE_ARM64.name, "arm64-apple-watchos", "watchos"),
        DarwinTarget(KonanTarget.WATCHOS_X64.name, "x86_64-apple-watchos-simulator", "watchsimulator"),
        DarwinTarget(KonanTarget.WATCHOS_SIMULATOR_ARM64.name, "arm64-apple-watchos-simulator", "watchsimulator"),
        DarwinTarget(KonanTarget.TVOS_ARM64.name, "arm64-apple-tvos", "appletvos"),
        DarwinTarget(KonanTarget.TVOS_X64.name, "x86_64-apple-tvos-simulator", "appletvsimulator"),
        DarwinTarget(KonanTarget.TVOS_SIMULATOR_ARM64.name, "arm64-apple-tvos-simulator", "appletvsimulator"),
        DarwinTarget(KonanTarget.MACOS_X64.name, "x86_64-apple-macos", "macosx"),
        DarwinTarget(KonanTarget.MACOS_ARM64.name, "arm64-apple-macos", "macosx"),
    )
