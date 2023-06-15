package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.SKIEGradlePluginPlugin
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.version.gradleApiVersions
import co.touchlab.skie.gradle.version.setupSourceSets
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.plugin.GradlePluginApiVersion
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinMultiplatformPluginWrapper
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverExtension
import org.jetbrains.kotlin.samWithReceiver.gradle.SamWithReceiverGradleSubplugin

class SkieGradle: Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        apply<KotlinMultiplatformPluginWrapper>()
        apply<SamWithReceiverGradleSubplugin>()
        apply<SKIEGradlePluginPlugin>()

        group = "co.touchlab.skie"

        extensions.configure<KotlinMultiplatformExtension> {
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

        extensions.configure<SamWithReceiverExtension> {
            annotation("org.gradle.api.HasImplicitReceiver")
        }
    }
}
