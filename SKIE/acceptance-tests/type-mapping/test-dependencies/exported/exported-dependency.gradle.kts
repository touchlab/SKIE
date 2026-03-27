plugins {
    id("tests.dependencies")
}

kotlin {
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    macosX64()
    macosArm64()
}
