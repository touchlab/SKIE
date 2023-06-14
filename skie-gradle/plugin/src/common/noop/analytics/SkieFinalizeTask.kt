package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.plugin.analytics.producer.AnalyticsCollector
import co.touchlab.skie.plugin.util.BaseSkieTask
import co.touchlab.skie.plugin.directory.skieDirectories
import org.gradle.api.BuildCancelledException
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

internal abstract class SkieFinalizeTask : BaseSkieTask() {

    @get:Internal
    abstract val linkTask: Property<KotlinNativeLink>

    @get:Internal
    abstract val analyticsCollector: Property<AnalyticsCollector>

    init {
        doNotTrackState("Must always run after link task.")
    }

    override fun runTask() {
        reportErrors()

        uploadAnalytics()
    }

    private fun reportErrors() {
        linkTask.get().state.failure?.let {
            if (!it.isCausedByBuildCancelledException) {
                analyticsCollector.get().logException(it)
            }
        }
    }

    private val Throwable.isCausedByBuildCancelledException: Boolean
        get() = this is BuildCancelledException || cause?.isCausedByBuildCancelledException == true

    private fun uploadAnalytics() {
        val analyticsDirectories = linkTask.get().skieDirectories.analyticsDirectories

        GradleAnalyticsManager.uploadAnalytics(analyticsCollector.get(), analyticsDirectories)
    }
}
