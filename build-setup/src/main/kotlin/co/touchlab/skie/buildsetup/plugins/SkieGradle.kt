package co.touchlab.skie.buildsetup.plugins

import co.touchlab.skie.gradle.version.gradleApiVersion
import co.touchlab.skie.gradle.version.gradleApiVersionDimension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetExtension
import co.touchlab.skie.gradle.version.target.MultiDimensionTargetPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.attributes.Bundling
import org.gradle.api.attributes.plugin.GradlePluginApiVersion
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named

abstract class SkieGradle : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<SkieBase>()
        apply<MultiDimensionTargetPlugin>()
        apply<DevGradleImplicitReceiver>()

        extensions.configure<MultiDimensionTargetExtension> {
            dimensions(gradleApiVersionDimension()) { target ->
                jvm(target.name) {
                    attributes {
                        attribute(GradlePluginApiVersion.GRADLE_PLUGIN_API_VERSION_ATTRIBUTE, objects.named(target.gradleApiVersion.value))
                        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
                    }
                }
            }

            configureSourceSet { sourceSet ->
                val gradleApiVersion = sourceSet.gradleApiVersion

                val gradleVersion = gradleApiVersion.value
                val kotlinVersion = gradleApiVersion.version.kotlinVersion.toString()

                dependencies {
                    weak("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
                    weak("dev.gradleplugins:gradle-api:$gradleVersion")
                }
            }
        }
    }
}
