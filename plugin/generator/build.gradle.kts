import co.touchlab.skie.gradle.extractedKotlinNativeCompilerEmbeddable
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `maven-publish`

    alias(libs.plugins.buildconfig)
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + listOf("-Xcontext-receivers")
    }
}

buildConfig {
    packageName(project.group.toString())

    val pluginId: String by properties
    buildConfigField("String", "PLUGIN_ID", "\"$pluginId\"")
}

dependencies {
    compileOnly(extractedKotlinNativeCompilerEmbeddable())
    implementation(projects.api)
    implementation(projects.spi)
    implementation(projects.configurationApi)
    implementation(projects.generator.configurationAnnotations)
    implementation(projects.generator.configurationGradle)
}
