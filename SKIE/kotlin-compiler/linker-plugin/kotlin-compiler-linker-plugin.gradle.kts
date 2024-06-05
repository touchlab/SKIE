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
                api(projects.kotlinCompiler.kotlinCompilerCore)

                api(projects.common.analytics)
                api(projects.common.configuration.configurationApi)
                implementation(projects.common.configuration.configurationAnnotations)
                implementation(projects.common.configuration.configurationInternal)
                api(projects.common.configuration.configurationDeclaration)
                api(projects.common.util)

                implementation(libs.kotlinx.coroutines.jvm)

                implementation(libs.kotlinx.serialization.json)
            }
        }
    }
}
