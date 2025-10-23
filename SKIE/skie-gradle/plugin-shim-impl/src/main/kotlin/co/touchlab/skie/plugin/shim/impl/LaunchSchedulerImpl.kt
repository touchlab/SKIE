@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package co.touchlab.skie.plugin.shim.impl

import co.touchlab.skie.plugin.shim.LaunchScheduler
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginLifecycle
import org.jetbrains.kotlin.gradle.plugin.launchInRequiredStage

class LaunchSchedulerImpl : LaunchScheduler {

    override fun whenMinOsVersionCanBeSafelyChanged(project: Project, block: () -> Unit) {
        project.launchInRequiredStage(KotlinPluginLifecycle.Stage.AfterFinaliseRefinesEdges) {
            block()
        }
    }
}
