package co.touchlab.skie.buildsetup.main.plugins.base

import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class BasePlugin : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        group = "co.touchlab.skie"
        version = System.getenv("RELEASE_VERSION").orEmpty().ifBlank { "1.0.0-SNAPSHOT" }
    }
}
