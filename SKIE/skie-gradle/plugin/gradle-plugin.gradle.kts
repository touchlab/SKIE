import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.util.gradlePluginApi
import co.touchlab.skie.gradle.version.gradleApiVersionDimension
import co.touchlab.skie.gradle.version.kotlinToolingVersionDimension

plugins {
    id("skie.gradle.plugin")
    id("skie.publishable")
}

skiePublishing {
    name = "SKIE Gradle Plugin"
    description = "Gradle plugin for configuring SKIE compiler plugin."
}

configurations.configureEach {
    attributes {
        attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named(gradleApiVersionDimension().components.min().value))
    }
}

dependencies {
    api(projects.gradle.gradlePluginApi)
    implementation(projects.gradle.gradlePluginImpl)

    compileOnly(gradlePluginApi())
}

tasks.named("compileKotlin").configure {
    val gradleApiVersions = project.gradleApiVersionDimension()
    val kotlinToolingVersions = project.kotlinToolingVersionDimension()

    gradleApiVersions.components.forEach { gradleApiVersion ->
        kotlinToolingVersions.components.forEach { kotlinToolingVersion ->
            val shimConfiguration = configurations.detachedConfiguration(
                projects.gradle.gradlePluginShimImpl,
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

@Suppress("UnstableApiUsage")
gradlePlugin {
    website = "https://skie.touchlab.co"
    vcsUrl = "https://github.com/touchlab/SKIE.git"

    this.plugins {
        create("co.touchlab.skie") {
            id = "co.touchlab.skie"
            displayName = "Swift and Kotlin, unified"
            implementationClass = "co.touchlab.skie.plugin.SkieGradlePlugin"
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
