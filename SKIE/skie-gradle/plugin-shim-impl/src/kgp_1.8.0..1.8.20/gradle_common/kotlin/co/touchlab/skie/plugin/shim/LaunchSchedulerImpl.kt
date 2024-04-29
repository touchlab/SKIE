package co.touchlab.skie.plugin.shim

import org.gradle.api.Project

class LaunchSchedulerImpl : LaunchScheduler {

    override fun Project.afterEvaluateOrAfterFinaliseRefinesEdges(block: () -> Unit) {
        afterEvaluate {
            block()
        }
    }
}

actual fun LaunchScheduler(): LaunchScheduler = LaunchSchedulerImpl()
