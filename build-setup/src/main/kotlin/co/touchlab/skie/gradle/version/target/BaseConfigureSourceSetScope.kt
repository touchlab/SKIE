package co.touchlab.skie.gradle.version.target

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

abstract class BaseConfigureSourceSetScope(

): ConfigureSourceSetScope {
    protected abstract val project: Project
    abstract override val kotlinSourceSet: KotlinSourceSet

    override fun configureRelatedConfigurations(block: Configuration.() -> Unit) {
        kotlinSourceSet.relatedConfigurationNames.forEach {
            project.configurations.named(it).configure(block)
        }
    }
}
