import co.touchlab.swiftgen.gradle.extractedKotlinNativeCompilerEmbeddable
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
    implementation(libs.swiftpack.api)
    implementation(libs.swiftlink.plugin.spi)
    implementation(project(":api"))
    implementation(project(":configuration"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
