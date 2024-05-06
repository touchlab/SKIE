@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package co.touchlab.skie.plugin.shim

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle
import org.jetbrains.kotlin.gradle.plugin.launchInRequiredStage

class LaunchSchedulerImpl : LaunchScheduler {

    override fun afterEvaluateOrAfterFinaliseRefinesEdges(project: Project, block: () -> Unit) {
        project.launchInRequiredStage(KotlinPluginLifecycle.Stage.AfterFinaliseRefinesEdges) {
            block()
        }
    }
}

actual fun LaunchScheduler(): LaunchScheduler = LaunchSchedulerImpl()
