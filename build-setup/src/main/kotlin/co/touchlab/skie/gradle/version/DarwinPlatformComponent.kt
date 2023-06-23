package co.touchlab.skie.gradle.version

import co.touchlab.skie.gradle.version.target.Target

enum class DarwinPlatformComponent: Target.Component {
    iosArm32,
    iosArm64,
    iosX64,
    iosSimulatorArm64,

    watchosArm32,
    watchosArm64,
    watchosX86,
    watchosX64,
    watchosSimulatorArm64,

    tvosArm64,
    tvosX64,
    tvosSimulatorArm64,

    macosX64,
    macosArm64;

    override val value: String get() = name
}

val Target.darwinPlatform: DarwinPlatformComponent
    get() = component()
