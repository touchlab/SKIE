package co.touchlab.skie.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class SkieGradlePlugin : Plugin<Project> {

    override fun apply(project: Project) {
        SkieGradlePluginApplier.apply(project)
    }
}
