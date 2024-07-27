package co.touchlab.skie.plugin.shim.impl

import co.touchlab.skie.plugin.shim.LaunchScheduler
import org.gradle.api.Project

class LaunchSchedulerImpl : LaunchScheduler {

    override fun whenMinOsVersionCanBeSafelyChanged(project: Project, block: () -> Unit) {
        if (project.state.executed) {
            block()
        } else {
            project.afterEvaluate {
                block()
            }
        }
    }
}

actual fun LaunchScheduler(): LaunchScheduler = LaunchSchedulerImpl()
