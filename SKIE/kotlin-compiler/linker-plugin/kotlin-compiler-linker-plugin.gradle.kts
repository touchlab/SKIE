plugins {
    id("skie.compiler.linker")
    id("utility.skie-publishable")
    id("utility.experimental.context-receivers")

    kotlin("plugin.serialization")
}

skiePublishing {
    name = "SKIE Kotlin compiler plugin"
    description = "Kotlin compiler plugin that improves Swift interface of a Kotlin Multiplatform framework."
}

dependencies {
    sharedApi(projects.kotlinCompiler.kotlinCompilerCore)

    sharedApi(projects.common.analytics)
    sharedApi(projects.common.configuration.configurationApi)
    sharedImplementation(projects.common.configuration.configurationAnnotations)
    sharedImplementation(projects.common.configuration.configurationInternal)
    sharedApi(projects.common.configuration.configurationDeclaration)
    sharedApi(projects.common.util)

    sharedImplementation(libs.kotlinx.coroutines.jvm)

    sharedImplementation(libs.kotlinx.serialization.json)
}

// Kotlin 2.4.20 annotated the K1 API used by the linker plugin with `K1Deprecation`, a RequiresOptIn marker with error level.
kotlin.sourceSets.configureEach {
    languageSettings.optIn("org.jetbrains.kotlin.K1Deprecation")
}
