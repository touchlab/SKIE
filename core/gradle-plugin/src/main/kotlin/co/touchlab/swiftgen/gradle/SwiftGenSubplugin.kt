package co.touchlab.swiftgen.gradle

import co.touchlab.swiftgen.BuildConfig
import co.touchlab.swiftlink.plugin.SwiftLinkSubplugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.provider.Provider
import org.gradle.configurationcache.extensions.capitalized
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import javax.inject.Inject

abstract class SwiftGenSubplugin @Inject constructor() : SwiftLinkSubplugin {

    override val compilerPluginId: String = BuildConfig.KOTLIN_PLUGIN_ID

    override fun apply(target: Project) {
        super.apply(target)

        target.extensions.create("swiftGen", SwiftGenExtension::class.java)

        target.plugins.withType<KotlinMultiplatformPlugin> {
            target.configure<KotlinMultiplatformExtension> {
                sourceSets.getByName("commonMain").dependencies {
                    implementation("co.touchlab.swiftgen:api:${BuildConfig.KOTLIN_PLUGIN_VERSION}")
                }
            }
        }
    }

    override fun getOptions(project: Project): List<SubpluginOption> {
        val pluginConfiguration = project.extensions.getByType<SwiftGenExtension>()

        return pluginConfiguration.toSubpluginOptions()
    }

    override fun configureDependencies(project: Project, pluginConfiguration: Configuration) {
        project.dependencies {
            pluginConfiguration("co.touchlab.swiftgen:api:${BuildConfig.KOTLIN_PLUGIN_VERSION}")
            pluginConfiguration("co.touchlab.swiftgen:configuration:${BuildConfig.KOTLIN_PLUGIN_VERSION}")
            pluginConfiguration(
                group = BuildConfig.KOTLIN_PLUGIN_GROUP,
                name = BuildConfig.KOTLIN_PLUGIN_NAME,
                version = BuildConfig.KOTLIN_PLUGIN_VERSION,
            )
        }
    }
}
