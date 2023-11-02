package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.plugin.util.SkieTarget
import co.touchlab.skie.plugin.analytics.environment.GradleEnvironmentAnalytics
import co.touchlab.skie.plugin.analytics.git.GitAnalytics
import co.touchlab.skie.plugin.analytics.hardware.HardwareAnalytics
import co.touchlab.skie.plugin.analytics.performance.GradlePerformanceAnalytics
import co.touchlab.skie.plugin.analytics.project.ProjectAnalytics
import co.touchlab.skie.plugin.configuration.SkieExtension.Companion.buildConfiguration
import co.touchlab.skie.plugin.configuration.skieExtension
import co.touchlab.skie.plugin.directory.createSkieBuildDirectoryTask
import co.touchlab.skie.plugin.util.*
import co.touchlab.skie.plugin.util.registerSkieTargetBasedTask
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import java.time.Duration

internal class GradleAnalyticsManager(
    private val project: Project,
) {

    fun configureAnalytics(target: SkieTarget) {
        val analyticsCollectorProvider = target.skieDirectories.map { skieDirectories ->
            AnalyticsCollector(
                skieBuildDirectory = skieDirectories.buildDirectory,
                skieConfiguration = project.skieExtension.buildConfiguration(),
            )
        }

        configureUploadAnalyticsTask(target)

        registerAnalyticsProducers(target, analyticsCollectorProvider)
    }

    private fun configureUploadAnalyticsTask(target: SkieTarget) {
        val uploadTask = target.registerSkieTargetBasedTask<SkieUploadAnalyticsTask>("uploadAnalytics") {
            this.analyticsDirectory.set(target.skieDirectories.map { it.buildDirectory.analytics.directory })
            this.applicationSupportDirectory.set(target.skieDirectories.map { it.applicationSupport })

            dependsOn(target.createSkieBuildDirectoryTask)

            onlyIf {
                val analyticsConfiguration = project.skieExtension.analytics

                analyticsConfiguration.enabled.get() && !analyticsConfiguration.disableUpload.get()
            }
        }

        target.task.configure {
            finalizedBy(uploadTask)
        }
    }

    private fun registerAnalyticsProducers(
        target: SkieTarget,
        analyticsCollectorProvider: Provider<AnalyticsCollector>,
    ) {
        target.task.configureDoFirstOptimized {
            analyticsCollectorProvider.get().collectAsync(
                GradleEnvironmentAnalytics.Producer(project),

                GitAnalytics.Producer(project),

                HardwareAnalytics.Producer,

                ProjectAnalytics.Producer(project),
            )
        }

        registerPerformanceAnalyticsProducer(target, analyticsCollectorProvider)
    }

    private fun registerPerformanceAnalyticsProducer(
        target: SkieTarget,
        analyticsCollectorProvider: Provider<AnalyticsCollector>,
    ) {
        var start: Long = 0

        target.task.configureDoFirstOptimized {
            start = System.currentTimeMillis()
        }

        target.task.configureDoLastOptimized {
            val linkTaskDuration = Duration.ofMillis(System.currentTimeMillis() - start)

            analyticsCollectorProvider.get().collectSynchronously(
                GradlePerformanceAnalytics.Producer(linkTaskDuration),
            )
        }
    }
}
