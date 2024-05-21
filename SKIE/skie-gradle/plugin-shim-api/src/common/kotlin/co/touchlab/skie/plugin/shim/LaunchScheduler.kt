package co.touchlab.skie.plugin.shim

import org.gradle.api.Project

interface LaunchScheduler {

    /**
     * Used to safely configure `osVersionMin` and `osVersionMinSinceXcode15` properties.
     * "Safely" here means "as late as possible",
     * making sure we can pick up overrides defined by users (or possibly other plugins).
     *
     * - KGP 1.8.0..1.8.20: Checks if `project` is already evaluated,
     *                      if so it runs `block` right away.
     *                      Otherwise, it schedules it with `project.afterEvaluate`.
     *                      ConfigureMinOsVersions at the time of this change already runs in `afterEvaluate`,
     *                      so there's no need to use it again.
     *                      It also runs inside a `configureEach` which disallows `afterEvaluate` calls.
     * - KGP >=1.9.0: Always schedules it using `launchInRequiredStage(KotlinPluginLifecycle.Stage.AfterFinaliseRefinesEdges)`.
     */
    fun whenMinOsVersionCanBeSafelyChanged(project: Project, block: () -> Unit)
}
