package co.touchlab.skie.plugin.util

import co.touchlab.skie.plugin.analytics.GradleAnalyticsManager
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

internal interface SkieTask : Task {

    @get:Internal
    val analyticsManager: Property<GradleAnalyticsManager>

    @TaskAction
    fun runTaskWithErrorHandling() {
        analyticsManager.get().withErrorLogging {
            runTask()
        }
    }

    fun runTask()
}

internal abstract class BaseSkieTask : DefaultTask(), SkieTask

internal inline fun <reified T : SkieTask> KotlinNativeLink.registerSkieLinkBasedTask(
    baseName: String,
    analyticsManager: GradleAnalyticsManager,
    crossinline configurationAction: T.() -> Unit,
): TaskProvider<T> {
    val taskNameWithoutPrefix = skieLinkTaskName(baseName).removePrefix("skie")

    return project.registerSkieTask(taskNameWithoutPrefix, analyticsManager, configurationAction)
}

internal inline fun <reified T : SkieTask> Project.registerSkieTask(
    baseName: String,
    analyticsManager: GradleAnalyticsManager,
    crossinline configurationAction: T.() -> Unit,
): TaskProvider<T> {
    val taskName = "skie${baseName.replaceFirstChar { it.uppercase() }}"

    return tasks.register(taskName, T::class.java) {
        it.group = "skie"

        it.analyticsManager.set(analyticsManager)

        it.configurationAction()
    }
}

internal fun KotlinNativeLink.skieLinkTaskName(baseName: String): String =
    "skie${baseName.replaceFirstChar { it.uppercase() }}${this.name.removePrefix("link")}"
