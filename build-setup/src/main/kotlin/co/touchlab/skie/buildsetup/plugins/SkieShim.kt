package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.KotlinCompilerVersion
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

abstract class SkieShim: Plugin<Project> {
    override fun apply(project: Project) = with(project) {
        apply<SkieBase>()
        apply<MultiDimensionTargetPlugin>()
        apply<DevGradleImplicitReceiver>()

        extensions.configure<MultiDimensionTargetExtension> {
            dimensions(kotlinToolingVersionDimension(), gradleApiVersionDimension()) { target ->
                jvm(target.name) {
                    attributes {
                        attribute(KotlinCompilerVersion.attribute, objects.named(target.kotlinToolingVersion.value))
                        attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named(target.gradleApiVersion.value))
                    }
                }
            }

            configureSourceSet { sourceSet ->
                val kotlinToolingVersion = sourceSet.kotlinToolingVersion.value
                val gradleApiVersion = sourceSet.gradleApiVersion.value
                val kotlinVersion = sourceSet.gradleApiVersion.version.kotlinVersion.toString()
                val groovyVersion = sourceSet.gradleApiVersion.version.groovyVersion

                dependencies {
                    weak("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
                    weak("dev.gradleplugins:gradle-api:$gradleApiVersion")
                    weak("org.codehaus.groovy:groovy-json:$groovyVersion")
                    weak("org.jetbrains.kotlin:kotlin-gradle-plugin-api:$kotlinToolingVersion")
                    weak("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinToolingVersion")
                }
            }
        }
    }
}
