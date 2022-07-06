package co.touchlab.swiftpack.plugin

import co.touchlab.swiftpack.BuildConfig
import co.touchlab.swiftpack.plugin.SwiftPack.pluginConfigurationName
import co.touchlab.swiftpack.plugin.SwiftPack.swiftTemplateDirectory
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

class SpecConfigGradleSubplugin: KotlinCompilerPluginSupportPlugin {
    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val target = kotlinCompilation.target
        return target is KotlinNativeTarget && target.konanTarget.family.isAppleFamily
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        val dependencies = listOf(
            "co.touchlab.swiftpack:swiftpack-api:${BuildConfig.KOTLIN_PLUGIN_VERSION}",
            "co.touchlab.swiftpack:swiftpack-spec:${BuildConfig.KOTLIN_PLUGIN_VERSION}",
            "io.outfoxx:swiftpoet:1.4.2",
            "org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.3.3",
            "org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.3.3",
        )

        dependencies.forEach { dependency ->
            project.configurations.getByName(kotlinCompilation.pluginConfigurationName).dependencies.add(
                project.dependencies.create(dependency)
            )
        }

        return project.provider {
            listOf(
                SubpluginOption(
                    SwiftPackConfigCommandLineProcessor.Options.outputDir,
                    project.swiftTemplateDirectory(kotlinCompilation.target).get().asFile.absolutePath,
                )
            )
        }
    }

    override fun getCompilerPluginId(): String = SwiftPackConfigCommandLineProcessor.pluginId

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = BuildConfig.KOTLIN_PLUGIN_GROUP,
        artifactId = BuildConfig.KOTLIN_PLUGIN_NAME,
        version = BuildConfig.KOTLIN_PLUGIN_VERSION,
    )

    override fun getPluginArtifactForNative(): SubpluginArtifact? = SubpluginArtifact(
        groupId = BuildConfig.KOTLIN_PLUGIN_GROUP,
        artifactId = BuildConfig.KOTLIN_PLUGIN_NAME,
        version = BuildConfig.KOTLIN_PLUGIN_VERSION,
    )
}