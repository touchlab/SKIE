package co.touchlab.skie.plugin.analytics

import co.touchlab.skie.plugin.analytics.producer.AnalyticsCollector
import co.touchlab.skie.plugin.analytics.producer.AnalyticsUploader
import co.touchlab.skie.plugin.util.skieDirectories
import org.gradle.api.BuildCancelledException
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

internal abstract class SkieFinalizeTask : DefaultTask() {

    abstract val linkTask: Property<KotlinNativeLink>

    abstract val analyticsCollector: Property<AnalyticsCollector>

    init {
        doNotTrackState("Must always run after link task.")
    }

    @TaskAction
    fun runTask() {
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
        val analyticsCollector = analyticsCollector.get()

        val directoryForUpload = linkTask.get().skieDirectories.analyticsDirectories.directoryWithFilesToUpload.toPath()

        AnalyticsUploader(analyticsCollector).sendAllIfPossible(directoryForUpload)
    }
}
