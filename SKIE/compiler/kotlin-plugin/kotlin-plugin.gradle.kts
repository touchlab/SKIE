plugins {
    id("skie.compiler")
    id("skie.publishable")
    id("dev.buildconfig")
    id("experimental.context-receivers")

    alias(libs.plugins.kotlin.plugin.serialization)
}

buildConfig {
    val kotlinPlugin = projects.compiler.kotlinPlugin.dependencyProject

    buildConfigField("String", "SKIE_VERSION", "\"${kotlinPlugin.version}\"")
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.common.analytics)
                implementation(projects.common.configuration)
                implementation(projects.common.license)
                implementation(projects.common.util)

                implementation(projects.runtime.runtimeSwift)

                implementation(libs.logback)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.serialization.yaml)

                implementation(libs.java.jwt)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.jackson.databind)
                implementation(libs.ktor.client.java)
            }
        }
    }
}
