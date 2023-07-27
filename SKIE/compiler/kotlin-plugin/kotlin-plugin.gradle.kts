plugins {
    id("skie.compiler")
    id("skie.publishable")
    id("experimental.context-receivers")

    kotlin("plugin.serialization")
}

skiePublishing {
    name = "SKIE Kotlin compiler plugin"
    description = "Kotlin compiler plugin that improves Swift interface of a Kotlin Multiplatform framework."
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(projects.common.analytics)
                api(projects.common.configuration.configurationApi)
                implementation(projects.common.configuration.configurationAnnotations)
                implementation(projects.common.configuration.configurationDeclaration)
                implementation(projects.common.util)

                implementation(projects.runtime.runtimeSwift)

                implementation(libs.logback)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.serialization.yaml)

                implementation(libs.kotlinx.serialization.json)
                implementation(libs.jackson.databind)
                implementation(libs.ktor.client.java)
                implementation(libs.apache.compress)
                implementation(libs.jgit)
            }
        }
    }
}
