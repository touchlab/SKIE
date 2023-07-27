plugins {
    id("skie.common")
    id("skie.publishable")
    id("dev.buildconfig")
}

skiePublishing {
    name = "SKIE Utils"
    description = "Common utils module for SKIE."
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
