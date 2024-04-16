package co.touchlab.skie.plugin.shim

import org.gradle.api.Project

interface LaunchScheduler {
    /**
     * KGP 1.8.0..1.8.20: behaves as `afterEvaluate`
     * KGP >=1.9.0: behaves as `launchInRequiredStage(KotlinPluginLifecycle.Stage.AfterFinaliseRefinesEdges)`
     */
    fun Project.afterEvaluateOrAfterFinaliseRefinesEdges(block: () -> Unit)
}
