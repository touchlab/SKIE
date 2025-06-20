package co.touchlab.skie.plugin.shim.impl.util

import co.touchlab.skie.util.TargetTriple
import org.jetbrains.kotlin.gradle.tasks.FrameworkDescriptor
import org.jetbrains.kotlin.konan.target.KonanTarget

data class DarwinTarget(
    val konanTarget: KonanTarget,
    val targetTriple: TargetTriple,
    val sdk: String,
) {

    constructor(
        konanTarget: KonanTarget,
        targetTripleString: String,
        sdk: String,
    ) : this(
        konanTarget,
        TargetTriple(targetTripleString), sdk,
    )

    companion object {

        val allTargets: Map<String, DarwinTarget> =
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
            ).associateBy { it.konanTarget.name }
    }
}

val FrameworkDescriptor.darwinTarget: DarwinTarget
    get() = target.darwinTarget

val KonanTarget.darwinTarget: DarwinTarget
    // We can't use `KotlinTarget` directly as the instance can differ when Gradle Configuration Cache is used
    get() = DarwinTarget.allTargets[name] ?: error("Unknown konan target: $this")
