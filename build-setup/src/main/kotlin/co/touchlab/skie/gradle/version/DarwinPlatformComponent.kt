package co.touchlab.skie.gradle.version

import co.touchlab.skie.gradle.version.target.Target

enum class DarwinPlatformComponent(
    val kotlinNativeTarget: String,
    val sinceKotlinVersion: String? = null,
    val untilKotlinVersionExclusive: String? = null,
) : Target.Component {

    iosArm64("ios_arm64"),
    iosX64("ios_x64"),
    iosSimulatorArm64("ios_simulator_arm64"),

    watchosArm32("watchos_arm32"),
    watchosArm64("watchos_arm64"),
    watchosDeviceArm64("watchos_device_arm64"),
    watchosX64("watchos_x64"),
    watchosSimulatorArm64("watchos_simulator_arm64"),

    tvosArm64("tvos_arm64"),
    tvosX64("tvos_x64"),
    tvosSimulatorArm64("tvos_simulator_arm64"),

    macosX64("macos_x64"),
    macosArm64("macos_arm64");

    override val value: String get() = name
}

val Target.darwinPlatform: DarwinPlatformComponent
    get() = component()
