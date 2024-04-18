@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package co.touchlab.skie.plugin.shim

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.launchInRequiredStage
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle

class LaunchSchedulerImpl() : LaunchScheduler {
    override fun Project.afterEvaluateOrAfterFinaliseRefinesEdges(block: () -> Unit) {
        project.launchInRequiredStage(KotlinPluginLifecycle.Stage.AfterFinaliseRefinesEdges) {
            block()
        }
    }
}

actual fun LaunchScheduler(): LaunchScheduler = LaunchSchedulerImpl()
