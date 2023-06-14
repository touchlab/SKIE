import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.SKIEGradlePluginPlugin
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.version.SourceSetScope
import co.touchlab.skie.gradle.version.kotlinPluginShimVersions
import co.touchlab.skie.gradle.version.setupSourceSets

plugins {
    kotlin("multiplatform")
    kotlin("plugin.sam.with.receiver")
}
apply<SKIEGradlePluginPlugin>()

group = "co.touchlab.skie"

KotlinCompilerVersion.registerIn(project)

kotlin {
    jvmToolchain(libs.versions.java)

    val kotlinShimVersions = project.kotlinPluginShimVersions()
    setupSourceSets(
        matrix = kotlinShimVersions,
        configureTarget = { cell ->
            attributes {
                attribute(KotlinCompilerVersion.attribute, objects.named(cell.kotlinToolingVersion.toString()))
                attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named(cell.gradleApiVersion.gradleVersion.version))
            }
        },
        configureSourceSet = {
            val shimVersion = target.baseValue

            val gradleVersion = shimVersion.gradleApiVersion.gradleVersion.version
            val kotlinVersion = shimVersion.gradleApiVersion.kotlinVersion.toString()
            val kotlinToolingVersion = shimVersion.kotlinToolingVersion.toString()

            addPlatform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion")
            addWeakDependency("org.jetbrains.kotlin:kotlin-stdlib", configureVersion(kotlinVersion))

            addWeakDependency("dev.gradleplugins:gradle-api", configureVersion(gradleVersion))

            addWeakDependency("org.jetbrains.kotlin:kotlin-native-compiler-embeddable", configureVersion(kotlinToolingVersion))

            kotlinSourceSet.relatedConfigurationNames.forEach {
                project.configurations.named(it).configure {
                    attributes {
                        attribute(KotlinCompilerVersion.attribute, objects.named(kotlinToolingVersion))
                        attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named(gradleVersion))
                    }
                }
            }
        }
    )
}

samWithReceiver {
    annotation("org.gradle.api.HasImplicitReceiver")
}
