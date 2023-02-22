plugins {
    id("skie-multiplatform")
}

kotlin {
    ios()

    val commonMain by sourceSets.getting {
        dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation("co.touchlab.skie:configuration-annotations")
            implementation("co.touchlab.skie:kotlin")
        }
    }
}
