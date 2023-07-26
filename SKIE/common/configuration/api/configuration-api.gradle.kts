plugins {
    id("skie.common")
    id("skie.publishable")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
            }
        }
    }
}
