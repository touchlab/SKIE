plugins {
    id("skie.compiler.core")
    id("skie.publishable")
    id("experimental.context-receivers")

    kotlin("plugin.serialization")
}

skiePublishing {
    name = "SKIE Kotlin compiler plugin core"
    description = "Module containing compiler independent code for SKIE compiler plugin."
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                // WIP Remove unused
                implementation(projects.common.analytics)
                api(projects.common.configuration.configurationApi)
                implementation(projects.common.configuration.configurationAnnotations)
                implementation(projects.common.configuration.configurationDeclaration)
                implementation(projects.common.util)

                implementation(libs.kotlinx.coroutines.jvm)
                implementation(projects.runtime.runtimeSwift)

                implementation(libs.logback)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.serialization.yaml)

                implementation(libs.jackson.databind)
                implementation(libs.ktor.client.java)
                implementation(libs.apache.compress)
            }
        }
    }
}
