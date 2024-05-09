package co.touchlab.skie.plugin.util

import co.touchlab.skie.plugin.SkieTarget
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider

fun SkieTarget.skieTargetBasedTaskName(baseName: String): String {
    val linkTaskNameWithoutPrefix = when (this) {
        is SkieTarget.Binary -> task.name.removePrefix("link")
        is SkieTarget.Artifact -> task.name.removePrefix("assemble")
    }
    return lowerCamelCaseName(
        "skie",
        baseName,
        linkTaskNameWithoutPrefix,
    )
}

inline fun <reified T : Task> SkieTarget.registerSkieTargetBasedTask(
    baseName: String,
    crossinline configurationAction: T.() -> Unit,
): TaskProvider<T> {
    val taskName = skieTargetBasedTaskName(baseName)

    return project.registerSkieTask(taskName, prefix = null, configurationAction = configurationAction)
}

inline fun <reified T : Task> Project.registerSkieTask(
    baseName: String,
    prefix: String? = "skie",
    crossinline configurationAction: T.() -> Unit,
): TaskProvider<T> {
    val taskName = lowerCamelCaseName(prefix, baseName)

    return tasks.register(taskName, T::class.java) {
        this.group = "skie"

        this.configurationAction()
    }
}
