plugins {
    id("skie.shim")
    id("skie.publishable")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                compileOnly(projects.kotlinGradlePluginShim)
            }
        }
    }
}
