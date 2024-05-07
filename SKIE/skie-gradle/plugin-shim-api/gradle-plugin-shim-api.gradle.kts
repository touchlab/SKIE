import co.touchlab.skie.gradle.version.gradleApiVersionDimension

plugins {
    id("skie.gradle")
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Gradle Plugin Shim API"
    description = "API that's implemented by the SKIE Gradle Plugin Shim Impl, used by the main plugin module to interact with Kotlin Gradle Plugin."
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.common.util)
                compileOnly("dev.gradleplugins:gradle-api:${gradleApiVersionDimension().components.min().value}")
            }
        }
    }
}
