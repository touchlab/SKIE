package co.touchlab.skie.gradle.debug

import org.gradle.api.Plugin
import org.gradle.api.Project

class GradleScriptDebugPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        target.afterEvaluate {

        }
    }
}
