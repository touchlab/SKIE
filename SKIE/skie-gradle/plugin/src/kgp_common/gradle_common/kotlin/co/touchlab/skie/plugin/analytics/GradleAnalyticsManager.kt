package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.plugin.analytics.environment.AnonymousGradleEnvironmentAnalytics
import co.touchlab.skie.plugin.analytics.git.AnonymousGitAnalytics
import co.touchlab.skie.plugin.analytics.git.IdentifyingGitAnalytics
import co.touchlab.skie.plugin.analytics.hardware.AnonymousHardwareAnalytics
import co.touchlab.skie.plugin.analytics.hardware.TrackingHardwareAnalytics
import co.touchlab.skie.plugin.analytics.performance.AnonymousGradlePerformanceAnalytics
import co.touchlab.skie.plugin.analytics.project.IdentifyingProjectAnalytics
import co.touchlab.skie.plugin.analytics.project.TrackingProjectAnalytics
import co.touchlab.skie.plugin.configuration.SkieExtension.Companion.buildConfiguration
import co.touchlab.skie.plugin.configuration.skieExtension
import co.touchlab.skie.plugin.directory.createSkieBuildDirectoryTask
import co.touchlab.skie.plugin.directory.skieDirectories
import co.touchlab.skie.plugin.util.doFirstOptimized
import co.touchlab.skie.plugin.util.doLastOptimized
import co.touchlab.skie.plugin.util.registerSkieLinkBasedTask
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.time.Duration

internal class GradleAnalyticsManager(
    private val project: Project,
) {

    fun configureAnalytics(linkTask: KotlinNativeLink) {
        val analyticsCollectorProvider = project.provider {
            AnalyticsCollector(
                skieBuildDirectory = linkTask.skieDirectories.buildDirectory,
                skieConfiguration = project.skieExtension.buildConfiguration(),
            )
        }

        configureUploadAnalyticsTask(linkTask)

        registerAnalyticsProducers(linkTask, analyticsCollectorProvider)
    }

    private fun configureUploadAnalyticsTask(linkTask: KotlinNativeLink) {
        val finalizeTask = linkTask.registerSkieLinkBasedTask<SkieUploadAnalyticsTask>("uploadAnalytics") {
            this.analyticsDirectory.set(linkTask.skieDirectories.buildDirectory.analytics.directory)

            dependsOn(linkTask.createSkieBuildDirectoryTask)
        }

        linkTask.finalizedBy(finalizeTask)
    }

    private fun registerAnalyticsProducers(
        linkTask: KotlinNativeLink,
        analyticsCollectorProvider: Provider<AnalyticsCollector>,
    ) {
        linkTask.doFirstOptimized {
            analyticsCollectorProvider.get().collectAsync(
                AnonymousGradleEnvironmentAnalytics.Producer(project),

                AnonymousGitAnalytics.Producer(project),
                IdentifyingGitAnalytics.Producer(project),

                TrackingHardwareAnalytics.Producer,
                AnonymousHardwareAnalytics.Producer,

                IdentifyingProjectAnalytics.Producer(project),
                TrackingProjectAnalytics.Producer(project),
            )
        }

        registerPerformanceAnalyticsProducer(linkTask, analyticsCollectorProvider)
    }

    private fun registerPerformanceAnalyticsProducer(
        linkTask: KotlinNativeLink,
        analyticsCollectorProvider: Provider<AnalyticsCollector>,
    ) {
        var start: Long = 0

        linkTask.doFirstOptimized {
            start = System.currentTimeMillis()
        }

        linkTask.doLastOptimized {
            val linkTaskDuration = Duration.ofMillis(System.currentTimeMillis() - start)

            analyticsCollectorProvider.get().collectSynchronously(
                AnonymousGradlePerformanceAnalytics.Producer(linkTaskDuration)
            )
        }
    }
}
