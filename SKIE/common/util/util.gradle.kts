plugins {
    id("skie.common")
    id("skie.publishable")
    id("dev.buildconfig")
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
            dependencies {
            }
        }
    }
}
