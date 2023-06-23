package co.touchlab.skie.gradle.version.target

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalModuleDependency
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

interface ConfigureSourceSetScope {
    val kotlinSourceSet: KotlinSourceSet

    val compilation: MultiDimensionTargetPlugin.Compilation

    fun addPlatform(notation: Any)

    fun addWeakDependency(dependency: String, configure: ExternalModuleDependency.() -> Unit = {})

    fun configureVersion(version: String): ExternalModuleDependency.() -> Unit

    fun configureRelatedConfigurations(block: Configuration.() -> Unit)
}
