plugins {
    id("skie-multiplatform")
}

kotlin {
    ios()
    macosX64()
    macosArm64()

    val commonMain by sourceSets.getting {
        dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation("co.touchlab.skie:configuration-annotations")
            implementation("co.touchlab.skie:kotlin")
        }
    }
}
