import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.publish.dependencyName
import co.touchlab.skie.gradle.version.gradleApiVersionDimension
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension

plugins {
    id("skie.gradle.plugin")
    id("skie.publishable")
    id("dev.buildconfig")
}

skiePublishing {
    name = "SKIE Gradle Plugin Loader"
    description = "Gradle plugin that loads the correct SKIE version based on Kotlin and Gradle versions."
}

buildConfig {
    val gradlePlugin = projects.gradle.gradlePlugin.dependencyProject
    // TODO Rename to SKIE_GRADLE_PLUGIN
    buildConfigField("String", "SKIE_GRADLE_PLUGIN_DEPENDENCY", "\"${gradlePlugin.dependencyName}\"")
}

kotlin {
    sourceSets {
        main {
            kotlin.srcDirs(
                "src/main/kotlin-compiler-attribute",
            )
        }
    }
}

configurations.configureEach {
    attributes {
        @Suppress("UnstableApiUsage")
        attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named(gradleApiVersionDimension().components.min().value))
    }
}

dependencies {
    implementation(projects.gradle.gradlePluginApi)
    api(projects.common.configuration.configurationDeclaration)
    compileOnly("dev.gradleplugins:gradle-api:${gradleApiVersionDimension().components.min().value}")

    testImplementation(kotlin("test"))
}

tasks.named("compileKotlin").configure {
    val gradleApiVersions = project.gradleApiVersionDimension()
    val kotlinToolingVersions = project.kotlinToolingVersionDimension()

    gradleApiVersions.components.forEach { gradleApiVersion ->
        kotlinToolingVersions.components.forEach { kotlinToolingVersion ->
            val shimConfiguration = configurations.detachedConfiguration(
                projects.gradle.gradlePlugin
            ).apply {
                attributes {
                    attribute(
                        KotlinCompilerVersion.attribute,
                        objects.named(KotlinCompilerVersion::class.java, kotlinToolingVersion.value),
                    )
                    attribute(
                        GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE,
                        objects.named(GradlePluginApiVersion::class.java, gradleApiVersion.value),
                    )
                }
            }
            dependsOn(shimConfiguration)
        }
    }
}

gradlePlugin {
    website = "https://skie.touchlab.co"
    vcsUrl = "https://github.com/touchlab/SKIE.git"

    this.plugins {
        create("co.touchlab.skie") {
            id = "co.touchlab.skie"
            displayName = "Swift and Kotlin, unified"
            implementationClass = "co.touchlab.skie.plugin.SkieLoaderPlugin"
            version = project.version

            description = "A Gradle plugin to add Swift into Kotlin/Native framework."
            tags = listOf(
                "swift",
                "kotlin",
                "native",
                "compiler",
            )
        }
    }
}
