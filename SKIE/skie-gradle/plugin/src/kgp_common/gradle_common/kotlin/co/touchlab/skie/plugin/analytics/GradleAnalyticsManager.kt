package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.plugin.analytics.producer.AnalyticsCollector
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

        configureAnalyticsTask(linkTask)

        configurePerformanceAnalytics(linkTask, analyticsCollectorProvider)
        collectGradleAnalytics(linkTask, analyticsCollectorProvider)
    }

    private fun configurePerformanceAnalytics(
        linkTask: KotlinNativeLink,
        analyticsCollectorProvider: Provider<AnalyticsCollector>,
    ) {
        var start: Long = 0

        linkTask.doFirstOptimized {
            start = System.currentTimeMillis()
        }

        linkTask.doLastOptimized {
            val linkTaskDuration = Duration.ofMillis(System.currentTimeMillis() - start)

            analyticsCollectorProvider.get().collect(GradlePerformanceAnalyticsProducer(linkTaskDuration))
        }
    }

    private fun configureAnalyticsTask(linkTask: KotlinNativeLink) {
        val finalizeTask = linkTask.registerSkieLinkBasedTask<SkieAnalyticsUploadTask>("analyticsUpload") {
            this.analyticsDirectory.set(linkTask.skieDirectories.buildDirectory.analytics.directory)

            dependsOn(linkTask.createSkieBuildDirectoryTask)
        }

        linkTask.finalizedBy(finalizeTask)
    }

    private fun collectGradleAnalytics(
        linkTask: KotlinNativeLink,
        analyticsCollectorProvider: Provider<AnalyticsCollector>,
    ) {
        linkTask.doFirstOptimized {
            analyticsCollectorProvider.get().collect(
                GradleAnalyticsProducer(project),
                OpenSourceAnalyticsProducer(project),
            )
        }
    }
}
