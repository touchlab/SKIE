plugins {
    id("skie-multiplatform")
    id("skie-publish-multiplatform")
}

kotlin {
    jvm()
    js(BOTH) {
        browser()
        nodejs()
    }

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX86()
    androidNativeX64()

    iosArm32()
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    watchosArm32()
    watchosArm64()
    watchosX86()
    watchosX64()
    watchosSimulatorArm64()

    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()

    macosX64()
    macosArm64()

    linuxArm64()
    linuxArm32Hfp()
    linuxX64()

    mingwX64()
    mingwX86()

    wasm32()
}
