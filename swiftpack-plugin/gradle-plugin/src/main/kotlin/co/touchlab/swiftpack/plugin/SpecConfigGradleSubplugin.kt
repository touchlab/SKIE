package co.touchlab.swiftpack.plugin

import co.touchlab.swiftpack.BuildConfig
import co.touchlab.swiftpack.plugin.SwiftPack.pluginConfigurationName
import co.touchlab.swiftpack.plugin.SwiftPack.swiftTemplateDirectory
import org.gradle.api.Project
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.exclude
import org.gradle.kotlin.dsl.named
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

const val SWIFT_PACK_PLUGIN_CONFIGURATION_NAME = "swiftPackPlugin"

class SpecConfigGradleSubplugin: KotlinCompilerPluginSupportPlugin {
    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val target = kotlinCompilation.target
        return target is KotlinNativeTarget && target.konanTarget.family.isAppleFamily
    }

    override fun apply(target: Project): Unit = with(target) {
        val swiftPackPluginConfiguration = configurations.maybeCreate(SWIFT_PACK_PLUGIN_CONFIGURATION_NAME).apply {
            isCanBeResolved = true
            isCanBeConsumed = false
            isVisible = false
            isTransitive = false

            exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")

            attributes {
                it.attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
                it.attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
                it.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
            }
        }
    }


    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project

        project.configurations.getByName(kotlinCompilation.pluginConfigurationName).extendsFrom(
            project.configurations.getByName(SWIFT_PACK_PLUGIN_CONFIGURATION_NAME),
        )

        return project.swiftTemplateDirectory(kotlinCompilation.target).map { templateDir ->
            listOf(
                SubpluginOption(
                    SwiftPackConfigCommandLineProcessor.Options.outputDir,
                    templateDir.asFile.absolutePath,
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
}
