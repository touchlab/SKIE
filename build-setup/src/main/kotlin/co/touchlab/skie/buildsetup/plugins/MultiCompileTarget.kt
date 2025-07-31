package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinToolingVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType

data class MultiCompileTarget(
    val name: String,
    val konanTargetName: String,
    val platformType: KotlinPlatformType = KotlinPlatformType.native,
    val declaration: (kotlinVersion: KotlinToolingVersion) -> String? = { "$name()" },
) {
    val capitalizedName: String
        get() = name.replaceFirstChar { it.uppercase() }

    companion object {
        val kotlin_2_0_0 = KotlinToolingVersion("2.0.0")
        val kotlin_2_1_0 = KotlinToolingVersion("2.1.0")

        val jvm = MultiCompileTarget(
            name = "jvm",
            konanTargetName = "jvm",
            platformType = KotlinPlatformType.jvm,
        )

        val js = MultiCompileTarget(
            name = "js",
            konanTargetName = "js",
            platformType = KotlinPlatformType.js,
            declaration = { kotlinVersion ->
                    """
                    js {
                        browser()
                        nodejs()
                    }
                    """.trimIndent()
            }
        )

        val wasmJs = MultiCompileTarget(
            name = "wasmJs",
            konanTargetName = "wasmJs",
            platformType = KotlinPlatformType.wasm,
        )

        val wasmWasi = MultiCompileTarget(
            name = "wasmWasi",
            konanTargetName = "wasmWasi",
            platformType = KotlinPlatformType.wasm,
        )

        val allWasm = listOf(
            wasmJs,
            wasmWasi,
        )

        val androidNativeArm32 = MultiCompileTarget(
            name = "androidNativeArm32",
            konanTargetName = "android_native_arm32",
        )
        val androidNativeArm64 = MultiCompileTarget(
            name = "androidNativeArm64",
            konanTargetName = "android_native_arm64",
        )
        val androidNativeX86 = MultiCompileTarget(
            name = "androidNativeX86",
            konanTargetName = "android_native_x86",
        )
        val androidNativeX64 = MultiCompileTarget(
            name = "androidNativeX64",
            konanTargetName = "android_native_x64",
        )

        val iosArm64 = MultiCompileTarget(
            name = "iosArm64",
            konanTargetName = "ios_arm64",
        )
        val iosX64 = MultiCompileTarget(
            name = "iosX64",
            konanTargetName = "ios_x64",
        )
        val iosSimulatorArm64 = MultiCompileTarget(
            name = "iosSimulatorArm64",
            konanTargetName = "ios_simulator_arm64",
        )

        val watchosArm32 = MultiCompileTarget(
            name = "watchosArm32",
            konanTargetName = "watchos_arm32",
        )
        val watchosArm64 = MultiCompileTarget(
            name = "watchosArm64",
            konanTargetName = "watchos_arm64",
        )
        val watchosX64 = MultiCompileTarget(
            name = "watchosX64",
            konanTargetName = "watchos_x64",
        )
        val watchosSimulatorArm64 = MultiCompileTarget(
            name = "watchosSimulatorArm64",
            konanTargetName = "watchos_simulator_arm64",
        )
        val watchosDeviceArm64 = MultiCompileTarget(
            name = "watchosDeviceArm64",
            konanTargetName = "watchos_device_arm64",
        )

        val tvosArm64 = MultiCompileTarget(
            name = "tvosArm64",
            konanTargetName = "tvos_arm64",
        )
        val tvosX64 = MultiCompileTarget(
            name = "tvosX64",
            konanTargetName = "tvos_x64",
        )
        val tvosSimulatorArm64 = MultiCompileTarget(
            name = "tvosSimulatorArm64",
            konanTargetName = "tvos_simulator_arm64",
        )

        val macosX64 = MultiCompileTarget(
            name = "macosX64",
            konanTargetName = "macos_x64",
        )
        val macosArm64 = MultiCompileTarget(
            name = "macosArm64",
            konanTargetName = "macos_arm64",
        )

        val linuxArm64 = MultiCompileTarget(
            name = "linuxArm64",
            konanTargetName = "linux_arm64",
        )
        val linuxArm32Hfp = MultiCompileTarget(
            name = "linuxArm32Hfp",
            konanTargetName = "linux_arm32_hfp",
        )
        val linuxX64 = MultiCompileTarget(
            name = "linuxX64",
            konanTargetName = "linux_x64",
        )

        val mingwX64 = MultiCompileTarget(
            name = "mingwX64",
            konanTargetName = "mingw_x64",
        )

        val allIos = listOf(
            iosArm64,
            iosX64,
            iosSimulatorArm64,
        )

        val allWatchos = listOf(
            watchosArm32,
            watchosArm64,
            watchosX64,
            watchosSimulatorArm64,
            watchosDeviceArm64,
        )

        val allTvos = listOf(
            tvosArm64,
            tvosX64,
            tvosSimulatorArm64,
        )

        val allMacos = listOf(
            macosX64,
            macosArm64,
        )

        val allDarwin = allIos + allWatchos + allTvos + allMacos

        val all = allDarwin + allWasm + listOf(
            jvm,
            js,
            androidNativeArm32,
            androidNativeArm64,
            androidNativeX86,
            androidNativeX64,
            linuxArm64,
            linuxArm32Hfp,
            linuxX64,
            mingwX64,
        )
    }
}
