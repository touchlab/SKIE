plugins {
    id("skie.compiler.core")
    id("utility.skie-publishable")
    id("utility.experimental.context-receivers")

    kotlin("plugin.serialization")
}

skiePublishing {
    name = "SKIE Kotlin compiler plugin core"
    description = "Module containing compiler independent code for SKIE compiler plugin."
}

dependencies {
    api(projects.common.analytics)
    api(projects.common.configuration.configurationApi)
    api(projects.common.configuration.configurationDeclaration)
    api(projects.common.util)

    implementation(projects.common.configuration.configurationInternal)

    implementation(libs.kotlinx.coroutines.jvm)
    implementation(projects.runtime.runtimeSwift)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.serialization.yaml)
}
