plugins {
    id("skie-multiplatform")
}

kotlin {
    ios()
    macosX64()
    macosArm64()

    val commonMain by sourceSets.getting {
        dependencies {
        }
    }
}
