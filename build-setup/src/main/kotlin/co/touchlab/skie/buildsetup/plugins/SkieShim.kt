package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinCompilerVersion
import co.touchlab.skie.gradle.SKIEGradlePluginPlugin
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.version.kotlinPluginShimVersions
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

class SkieShim: Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        apply<KotlinMultiplatformPluginWrapper>()
        apply<SamWithReceiverGradleSubplugin>()

        group = "co.touchlab.skie"

        KotlinCompilerVersion.registerIn(project)

        extensions.configure<KotlinMultiplatformExtension> {
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

        extensions.configure<SamWithReceiverExtension> {
            annotation("org.gradle.api.HasImplicitReceiver")
        }
    }
}
