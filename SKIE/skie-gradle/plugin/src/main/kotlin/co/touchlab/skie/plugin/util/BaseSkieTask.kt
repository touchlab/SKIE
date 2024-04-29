package co.touchlab.skie.plugin.util

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeArtifact
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeFatFramework
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeFramework
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeLibrary
import org.jetbrains.kotlin.gradle.dsl.KotlinNativeXCFramework

internal fun SkieTarget.skieTargetBasedTaskName(baseName: String): String {
    val linkTaskNameWithoutPrefix = when (this) {
        is SkieTarget.TargetBinary -> task.name.removePrefix("link")
        is SkieTarget.Artifact -> task.name.removePrefix("assemble")
    }
    return lowerCamelCaseName(
        "skie",
        baseName,
        linkTaskNameWithoutPrefix,
    )
}

internal fun Project.skieTargetsOf(artifact: KotlinNativeArtifact): List<SkieTarget> {
    return when (artifact) {
        is KotlinNativeLibrary -> if (artifact.target.family.isAppleFamily) {
            artifact.modes.map { buildType ->
                SkieTarget.Artifact(
                    project = this,
                    artifact = artifact,
                    konanTarget = artifact.target,
                    buildType = buildType,
                    outputKind = SkieTarget.OutputKind.Library,
                )
            }
        } else {
            emptyList()
        }

        is KotlinNativeFramework -> if (artifact.target.family.isAppleFamily) {
            artifact.modes.map { buildType ->
                SkieTarget.Artifact(
                    project = this,
                    artifact = artifact,
                    konanTarget = artifact.target,
                    buildType = buildType,
                    outputKind = SkieTarget.OutputKind.Framework,
                )
            }
        } else {
            emptyList()
        }

        is KotlinNativeFatFramework -> artifact.modes.flatMap { buildType ->
            artifact.targets.filter { it.family.isAppleFamily }.map { target ->
                SkieTarget.Artifact(
                    project = this,
                    artifact = artifact,
                    konanTarget = target,
                    buildType = buildType,
                    outputKind = SkieTarget.OutputKind.UniversalFramework,
                )
            }
        }

        is KotlinNativeXCFramework -> artifact.modes.flatMap { buildType ->
            artifact.targets.filter { it.family.isAppleFamily }.map { target ->
                SkieTarget.Artifact(
                    project = this,
                    artifact = artifact,
                    konanTarget = target,
                    buildType = buildType,
                    outputKind = SkieTarget.OutputKind.XCFramework,
                )
            }
        }
        else -> error("Unknown KotlinNativeArtifact type: $this")
    }
}

internal fun lowerCamelCaseName(vararg nameParts: String?): String {
    val nonEmptyParts = nameParts.mapNotNull { it?.takeIf(String::isNotEmpty) }
    return nonEmptyParts.drop(1).joinToString(
        separator = "",
        prefix = nonEmptyParts.firstOrNull().orEmpty(),
        transform = String::capitalizeAsciiOnly,
    )
}

private fun String.capitalizeAsciiOnly(): String {
    if (isEmpty()) return this
    val c = this[0]
    return if (c in 'a'..'z')
        c.uppercaseChar() + substring(1)
    else
        this
}

internal inline fun <reified T : Task> SkieTarget.registerSkieTargetBasedTask(
    baseName: String,
    crossinline configurationAction: T.() -> Unit,
): TaskProvider<T> {
    val taskName = skieTargetBasedTaskName(baseName)

    return project.registerSkieTask(taskName, prefix = null, configurationAction = configurationAction)
}

internal inline fun <reified T : Task> Project.registerSkieTask(
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
