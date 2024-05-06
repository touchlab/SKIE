package co.touchlab.skie.plugin.shim

import org.gradle.api.Project

class LaunchSchedulerImpl : LaunchScheduler {

    override fun afterEvaluateOrAfterFinaliseRefinesEdges(project: Project, block: () -> Unit) {
        project.afterEvaluate {
            block()
        }
    }
}

actual fun LaunchScheduler(): LaunchScheduler = LaunchSchedulerImpl()
