plugins {
    id("skie-multiplatform")
}

kotlin {
    ios()

    val commonMain by sourceSets.getting {
        dependencies {
            implementation("co.touchlab.skie:configuration-annotations")
        }
    }
}
