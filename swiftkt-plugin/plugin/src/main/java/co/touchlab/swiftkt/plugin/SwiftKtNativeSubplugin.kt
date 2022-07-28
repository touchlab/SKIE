package co.touchlab.swikt.plugin

import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import co.touchlab.swiftkt.BuildConfig
import co.touchlab.swiftkt.plugin.PluginOption
import co.touchlab.swiftkt.plugin.SwiftKtCommandLineProcessor
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

class SwiftKtNativeSubplugin: KotlinCompilerPluginSupportPlugin {
    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val nativeTarget = kotlinCompilation.target as? KotlinNativeTarget ?: return project.provider { emptyList() }

        return project.provider {
            listOf(

            )
        }
    }

    override fun getCompilerPluginId(): String {
        return "co.touchlab.swiftkt"
    }

    override fun getPluginArtifact(): SubpluginArtifact =
        SubpluginArtifact(groupId = BuildConfig.KOTLIN_PLUGIN_GROUP, artifactId = BuildConfig.KOTLIN_PLUGIN_NAME, version = BuildConfig.KOTLIN_PLUGIN_VERSION)

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return false // kotlinCompilation is KotlinNativeCompilation && kotlinCompilation.target.konanTarget.family.isAppleFamily
    }
}

fun <T> PluginOption<T>.subpluginOption(value: T): SubpluginOption = SubpluginOption(optionName, serialize(value))
