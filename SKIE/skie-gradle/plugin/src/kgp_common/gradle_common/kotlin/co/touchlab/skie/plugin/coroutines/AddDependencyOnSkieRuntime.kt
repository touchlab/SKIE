package co.touchlab.skie.plugin.coroutines

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle_plugin.BuildConfig
import co.touchlab.skie.plugin.util.named
import co.touchlab.skie.plugin.util.withType
import org.jetbrains.kotlin.gradle.plugin.mpp.AbstractNativeLibrary
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.konan.target.KonanTarget

internal fun KotlinNativeTarget.addDependencyOnSkieRuntime() {
    if (!project.isCoroutinesInteropEnabled) {
        return
    }

    compilations.named("main") {
        defaultSourceSet.dependencies {
            api(BuildConfig.RUNTIME_DEPENDENCY(konanTarget, BuildConfig.KOTLIN_TOOLING_VERSION))
        }
    }

    binaries.withType<AbstractNativeLibrary>().configureEach {
        export(BuildConfig.RUNTIME_DEPENDENCY(konanTarget, BuildConfig.KOTLIN_TOOLING_VERSION))
    }
}

private fun BuildConfig.RUNTIME_DEPENDENCY(konanTarget: KonanTarget, kotlinVersion: String): String {
    return "$RUNTIME_DEPENDENCY_GROUP:$RUNTIME_DEPENDENCY_NAME-${konanTarget.presetName}__kgp_${kotlinVersion}:$RUNTIME_DEPENDENCY_VERSION"
}

private val KonanTarget.presetName: String
    get() = when (this) {
        KonanTarget.IOS_ARM32 -> "iosArm32"
        KonanTarget.IOS_ARM64 -> "iosArm64"
        KonanTarget.IOS_X64 -> "iosX64"
        KonanTarget.IOS_SIMULATOR_ARM64 -> "iosSimulatorArm64"

        KonanTarget.MACOS_ARM64 -> "macosArm64"
        KonanTarget.MACOS_X64 -> "macosX64"

        KonanTarget.TVOS_ARM64 -> "tvosArm64"
        KonanTarget.TVOS_SIMULATOR_ARM64 -> "tvosSimulatorArm64"
        KonanTarget.TVOS_X64 -> "tvosX64"

        KonanTarget.WATCHOS_ARM32 -> "watchosArm32"
        KonanTarget.WATCHOS_ARM64 -> "watchosArm64"
        KonanTarget.WATCHOS_DEVICE_ARM64 -> "watchosArm64"
        KonanTarget.WATCHOS_X86 -> "watchosX86"
        KonanTarget.WATCHOS_X64 -> "watchosX64"
        KonanTarget.WATCHOS_SIMULATOR_ARM64 -> "watchosSimulatorArm64"

        KonanTarget.ANDROID_ARM32, KonanTarget.ANDROID_ARM64, KonanTarget.ANDROID_X64, KonanTarget.ANDROID_X86, KonanTarget.LINUX_ARM32_HFP,
        KonanTarget.LINUX_ARM64, KonanTarget.LINUX_MIPS32, KonanTarget.LINUX_MIPSEL32, KonanTarget.LINUX_X64, KonanTarget.MINGW_X64,
        KonanTarget.MINGW_X86, KonanTarget.WASM32, is KonanTarget.ZEPHYR -> error(
            "SKIE doesn't support these platforms, so it should never ask for the preset name of this target."
        )
    }
