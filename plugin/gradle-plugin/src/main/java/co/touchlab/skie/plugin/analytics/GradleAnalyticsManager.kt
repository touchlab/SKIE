package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.gradle_plugin.BuildConfig
import co.touchlab.skie.plugin.analytics.crash.BugsnagFactory
import co.touchlab.skie.plugin.analytics.producer.AnalyticsCollector
import co.touchlab.skie.plugin.configuration.SkieConfigurationProvider
import co.touchlab.skie.plugin.license.GradleSkieLicenseManager
import co.touchlab.skie.plugin.util.doFirstOptimized
import co.touchlab.skie.plugin.util.doLastOptimized
import co.touchlab.skie.plugin.util.registerSkieLinkBasedTask
import co.touchlab.skie.plugin.util.skieDirectories
import co.touchlab.skie.util.Environment
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.time.Duration
import java.util.UUID

internal class GradleAnalyticsManager(
    private val project: Project,
    private val licenseManager: GradleSkieLicenseManager,
) {

    val buildId: String = UUID.randomUUID().toString()

    fun withErrorLogging(action: () -> Unit) {
        try {
            action()
        } catch (e: Throwable) {
            BugsnagFactory.create(
                skieVersion = BuildConfig.KOTLIN_PLUGIN_VERSION,
                type = BugsnagFactory.Type.Gradle,
                environment = licenseManager.licenseOrNull?.environment ?: Environment.Unknown,
            )

            throw e
        }
    }

    fun configureAnalytics(linkTask: KotlinNativeLink) {
        val analyticsCollectorProvider = licenseManager.license.map { license ->
            AnalyticsCollector(
                analyticsDirectories = linkTask.skieDirectories.analyticsDirectories,
                buildId = buildId,
                skieVersion = BuildConfig.KOTLIN_PLUGIN_VERSION,
                type = BugsnagFactory.Type.Gradle,
                environment = license.environment,
                configuration = SkieConfigurationProvider.getConfiguration(linkTask.skieDirectories, license).analyticsConfiguration,
            )
        }

        configurePerformanceAnalytics(linkTask, analyticsCollectorProvider)
        collectGradleAnalytics(linkTask, analyticsCollectorProvider)
        configureAnalyticsTask(linkTask, analyticsCollectorProvider)
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

            analyticsCollectorProvider.get().collect(
                GradlePerformanceAnalyticsProducer(linkTaskDuration),
            )
        }
    }

    private fun collectGradleAnalytics(
        linkTask: KotlinNativeLink,
        analyticsCollectorProvider: Provider<AnalyticsCollector>,
    ) {
        linkTask.doFirstOptimized {
            analyticsCollectorProvider.get().collect(
                GradleAnalyticsProducer(project, licenseManager.license.get()),
            )
        }
    }

    private fun configureAnalyticsTask(linkTask: KotlinNativeLink, analyticsCollectorProvider: Provider<AnalyticsCollector>) {
        val finalizeTask = linkTask.registerSkieLinkBasedTask<SkieFinalizeTask>("finalize") {
            this.linkTask.set(linkTask)
            analyticsCollector.set(analyticsCollectorProvider)
        }

        linkTask.finalizedBy(finalizeTask)
    }
}
