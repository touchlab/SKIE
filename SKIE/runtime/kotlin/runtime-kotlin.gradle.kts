plugins {
    id("skie.runtime.kotlin")
    id("utility.skie-publishable")
}

skiePublishing {
    name = "SKIE Runtime - Kotlin"
    description = "Kotlin Multiplatform part of the SKIE runtime. It's used to facilitate certain features of SKIE."
}

kotlin {
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    watchosArm32()
    watchosArm64()
    watchosX64()
    watchosSimulatorArm64()
    watchosDeviceArm64()

    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()

    macosX64()
    macosArm64()

    sourceSets.configureEach {
        languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
    }
}

dependencies {
    commonMainImplementation(libs.kotlinx.coroutines.core)
}
