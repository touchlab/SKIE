package co.touchlab.skie.plugin.subplugin

import co.touchlab.skie.plugin.util.SkieTarget
import co.touchlab.skie.plugin.util.exclude
import co.touchlab.skie.plugin.util.named
import co.touchlab.skie.plugin.util.withType
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.attributes.Category
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage

internal object SkieSubPluginManager {

    private const val subPluginConfigurationName: String = "skieSubPlugin"

    fun configureDependenciesForSubPlugins(project: Project) {
        val subPluginConfiguration = project.configurations.create(subPluginConfigurationName).apply {
            isCanBeResolved = true
            isCanBeConsumed = false
            isVisible = false
            isTransitive = true

            exclude("org.jetbrains.kotlin", "kotlin-stdlib-common")

            attributes {
                attribute(Category.CATEGORY_ATTRIBUTE, project.objects.named(Category.LIBRARY))
                attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage.JAVA_RUNTIME))
                attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements.JAR))
            }
        }

        project.skieSubPlugins.configureEach {
            configureDependencies(project, subPluginConfiguration)
        }
    }

    fun registerSubPlugins(target: SkieTarget) {
        target.registerSubPluginsOptions()

        target.registerSubPluginsToClasspath()
    }

    private fun SkieTarget.registerSubPluginsOptions() {
        project.skieSubPlugins.forEach { subPlugin ->
            val options = subPlugin.getOptions(project, this)

            options.get().forEach {
                addPluginArgument(subPlugin.compilerPluginId, it)
            }
        }
    }

    private fun SkieTarget.registerSubPluginsToClasspath() {
        addToCompilerClasspath(project.configurations.getByName(subPluginConfigurationName))
    }
}

private val Project.skieSubPlugins: DomainObjectCollection<SkieSubplugin>
    get() = plugins.withType()
