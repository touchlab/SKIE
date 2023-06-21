plugins {
    id("skie.common")
    id("skie.publishable")
    id("dev.buildconfig")
    // Util probably shouldn't need serialization
    alias(libs.plugins.kotlin.plugin.serialization)
}

buildConfig {
    useKotlinOutput {
        internalVisibility = false
    }
    buildConfigField("String", "SKIE_VERSION", "\"${project.version}\"")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            // TODO: Util probably shouldn't bring the whole serialization library
            dependencies {
                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
