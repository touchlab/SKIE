
import co.touchlab.skie.gradle.SKIEGradlePluginPlugin
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.version.SourceSetScope
import co.touchlab.skie.gradle.version.gradleApiVersions
import co.touchlab.skie.gradle.version.setupSourceSets
import org.gradle.api.internal.plugins.PluginDescriptor
import org.gradle.internal.component.external.model.DefaultImmutableCapability

plugins {
    kotlin("multiplatform")
    kotlin("plugin.sam.with.receiver")
//     id("java-gradle-plugin")
}
apply<SKIEGradlePluginPlugin>()

group = "co.touchlab.skie"

kotlin {
    jvmToolchain(libs.versions.java)

    val gradleVersions = project.gradleApiVersions()
    setupSourceSets(
        matrix = gradleVersions,
        configureTarget = { cell ->
            attributes {
                println("Gradle version attribute: ${cell.gradleVersion.version}")
                attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named(cell.gradleVersion.version))
            }
        },
        configureSourceSet = {
            val gradleApi = target.baseValue

            val gradleVersion = gradleApi.gradleVersion.version
            val kotlinVersion = gradleApi.kotlinVersion.toString()

            addPlatform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion")
            addWeakDependency("org.jetbrains.kotlin:kotlin-stdlib", configureVersion(kotlinVersion))
            addWeakDependency("dev.gradleplugins:gradle-api", configureVersion(gradleVersion))

            kotlinSourceSet.relatedConfigurationNames.forEach {
                project.configurations.named(it).configure {
                    attributes {
                        attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named(gradleVersion))
                    }
                }
            }
        },
    )
}

samWithReceiver {
    annotation("org.gradle.api.HasImplicitReceiver")
}

// Disabling this task, so "com.gradle.plugin-publish" will not publish unshadowed jar into Gradle Plugin Portal
// Without it 'jar' task is asked to run by "com.gradle.plugin-publish" even if artifacts are removed. The problem
// is that 'jar' task runs after shadow task plus their outputs has the same name leading to '.jar' file overwrite.
// tasks.named("jar") {
//     enabled = false
// }
