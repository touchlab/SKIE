import co.touchlab.skie.gradle.util.extractedKotlinNativeCompilerEmbeddable

plugins {
    id("skie-jvm")
    id("skie-publish-jvm")
    id("skie-buildconfig")
}

buildConfig {
    val pluginId: String by properties
    buildConfigField("String", "PLUGIN_ID", "\"$pluginId\"")
}

skieJvm {
    areContextReceiversEnabled.set(true)
}

dependencies {
    compileOnly(extractedKotlinNativeCompilerEmbeddable())
    implementation(projects.api)
    implementation(projects.spi)
    api(projects.configurationApi)
    implementation(projects.generator.configurationAnnotations)
    implementation(projects.generator.configurationGradle)
    implementation(projects.runtime.swift)
}
