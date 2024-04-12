package co.touchlab.skie.test.util

@Suppress("EnumEntryName")
enum class RawKotlinTarget(val target: KotlinTarget) {
    androidTarget(KotlinTarget.AndroidTarget),
    iosArm64(KotlinTarget.Native.Ios.Arm64),
    iosX64(KotlinTarget.Native.Ios.X64),
    iosSimulatorArm64(KotlinTarget.Native.Ios.SimulatorArm64),
    macosArm64(KotlinTarget.Native.MacOS.Arm64),
    macosX64(KotlinTarget.Native.MacOS.X64),
}
