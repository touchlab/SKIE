package co.touchlab.skie.gradle.version.target

import org.gradle.api.artifacts.Configuration
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

interface ConfigureSourceSetScope {

    val kotlinSourceSet: KotlinSourceSet
    val compilation: MultiDimensionTargetPlugin.Compilation

    fun dependencies(block: MultiDimensionTargetDependencyHandler.() -> Unit)

    fun configureRelatedConfigurations(block: Configuration.() -> Unit)
}
