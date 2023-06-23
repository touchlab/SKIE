package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.SKIEGradlePluginPlugin
import co.touchlab.skie.gradle.util.libs
import co.touchlab.skie.gradle.version.*
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetExtension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetPlugin
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

abstract class SkieGradle: Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        apply<SkieBase>()
        apply<MultiDimensionTargetPlugin>()
        apply<DevGradleImplicitReceiver>()
        apply<SKIEGradlePluginPlugin>()

        extensions.configure<MultiDimensionTargetExtension> {
            dimensions.add(gradleApiVersionDimension())

            createTarget { target ->
                jvm(target.name) {
                    attributes {
                        attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named(target.gradleApiVersion.value))
                    }
                }
            }

            configureSourceSet { sourceSet ->
                val gradleApiVersion = sourceSet.gradleApiVersion

                val gradleVersion = gradleApiVersion.value
                val kotlinVersion = gradleApiVersion.version.kotlinVersion.toString()

                addPlatform("org.jetbrains.kotlin:kotlin-bom:$kotlinVersion")
                addWeakDependency("org.jetbrains.kotlin:kotlin-stdlib", configureVersion(kotlinVersion))
                addWeakDependency("dev.gradleplugins:gradle-api", configureVersion(gradleVersion))

                configureRelatedConfigurations {
                    attributes {
                        attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named(gradleApiVersion.value))
                    }
                }
            }
        }
    }
}
