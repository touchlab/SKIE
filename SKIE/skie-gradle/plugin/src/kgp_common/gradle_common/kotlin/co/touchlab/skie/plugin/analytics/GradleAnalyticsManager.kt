package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.gradle_plugin.BuildConfig
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsConfiguration
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature
import co.touchlab.skie.plugin.analytics.producer.AnalyticsCollector
import co.touchlab.skie.plugin.analytics.producer.AnalyticsUploader
import co.touchlab.skie.plugin.directory.createSkieBuildDirectoryTask
import co.touchlab.skie.plugin.directory.skieDirectories
import co.touchlab.skie.plugin.util.doFirstOptimized
import co.touchlab.skie.plugin.util.doLastOptimized
import co.touchlab.skie.plugin.util.registerSkieLinkBasedTask
import co.touchlab.skie.util.Environment
import co.touchlab.skie.util.directory.SkieAnalyticsDirectories
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.time.Duration
import java.util.UUID

internal class GradleAnalyticsManager(
    private val project: Project,
) {

    val buildId: String = UUID.randomUUID().toString()

    fun configureAnalytics(linkTask: KotlinNativeLink) {
        val analyticsCollector = AnalyticsCollector(
            analyticsDirectories = linkTask.skieDirectories.analyticsDirectories,
            buildId = buildId,
            skieVersion = BuildConfig.KOTLIN_PLUGIN_VERSION,
            configuration = AnalyticsConfiguration(
                AnalyticsFeature.GradlePerformance(true),
                AnalyticsFeature.CrashReporting(true),
                AnalyticsFeature.OpenSource(true),
                AnalyticsFeature.Gradle(true, true),
            ),
        )

        configureAnalyticsTask(linkTask, analyticsCollector)

        configurePerformanceAnalytics(linkTask, analyticsCollector)
        collectGradleAnalytics(linkTask, analyticsCollector)
    }

    private fun configurePerformanceAnalytics(
        linkTask: KotlinNativeLink,
        analyticsCollector: AnalyticsCollector,
    ) {
        var start: Long = 0

        linkTask.doFirstOptimized {
            start = System.currentTimeMillis()
        }

        linkTask.doLastOptimized {
            val linkTaskDuration = Duration.ofMillis(System.currentTimeMillis() - start)

            analyticsCollector.collect(GradlePerformanceAnalyticsProducer(linkTaskDuration))
        }
    }

    private fun configureAnalyticsTask(linkTask: KotlinNativeLink, analyticsCollector: AnalyticsCollector) {
        val finalizeTask = linkTask.registerSkieLinkBasedTask<SkieAnalyticsTask>("finalize", this) {
            this.linkTask.set(linkTask)
            this.analyticsCollector.set(analyticsCollector)

            dependsOn(linkTask.createSkieBuildDirectoryTask)
        }

        linkTask.finalizedBy(finalizeTask)
    }

    private fun configureAnalyticsSecondaryUpload(linkTask: KotlinNativeLink, analyticsCollector: AnalyticsCollector) {
        linkTask.doFirstOptimized {
            uploadAnalytics(analyticsCollector, linkTask.skieDirectories.analyticsDirectories)
        }
    }

    private fun collectGradleAnalytics(
        linkTask: KotlinNativeLink,
        analyticsCollector: AnalyticsCollector,
    ) {
        linkTask.doFirstOptimized {
            analyticsCollector.collect(
                GradleAnalyticsProducer(project),
                OpenSourceAnalyticsProducer(project),
            )
        }
    }

    companion object {

        fun uploadAnalytics(analyticsCollector: AnalyticsCollector, analyticsDirectories: SkieAnalyticsDirectories) {
            val directoryWithFilesToUpload = analyticsDirectories.directoryWithFilesToUpload.toPath()

            AnalyticsUploader(analyticsCollector).sendAll(directoryWithFilesToUpload)
        }
    }
}
