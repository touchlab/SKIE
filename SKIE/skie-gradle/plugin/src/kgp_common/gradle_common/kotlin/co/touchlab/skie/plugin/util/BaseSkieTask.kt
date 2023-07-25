package co.touchlab.skie.plugin.util

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink

internal inline fun <reified T : Task> KotlinNativeLink.registerSkieLinkBasedTask(
    baseName: String,
    crossinline configurationAction: T.() -> Unit,
): TaskProvider<T> {
    val taskNameWithoutPrefix = skieLinkTaskName(baseName).removePrefix("skie")

    return project.registerSkieTask(taskNameWithoutPrefix, configurationAction)
}

internal inline fun <reified T : Task> Project.registerSkieTask(
    baseName: String,
    crossinline configurationAction: T.() -> Unit,
): TaskProvider<T> {
    val taskName = "skie${baseName.replaceFirstChar { it.uppercase() }}"

    return tasks.register(taskName, T::class.java) {
        this.group = "skie"

        this.configurationAction()
    }
}

internal fun KotlinNativeLink.skieLinkTaskName(baseName: String): String =
    "skie${baseName.replaceFirstChar { it.uppercase() }}${this.name.removePrefix("link")}"
