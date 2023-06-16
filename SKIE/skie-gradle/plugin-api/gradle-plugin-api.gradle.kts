import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.publish.dependencyName
import co.touchlab.skie.gradle.publish.mavenArtifactId
import co.touchlab.skie.gradle.version.gradleApiVersions
import co.touchlab.skie.gradle.version.kotlinToolingVersions

plugins {
    id("skie.gradle")
    id("skie.publishable")
    id("dev.buildconfig")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.common.configuration)
            }
        }
    }
}
