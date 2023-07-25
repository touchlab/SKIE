package co.touchlab.skie.plugin.directory

import co.touchlab.skie.plugin.util.registerSkieLinkBasedTask
import co.touchlab.skie.plugin.util.skieLinkTaskName
import co.touchlab.skie.util.directory.SkieBuildDirectory
import co.touchlab.skie.util.directory.SkieDirectories
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import javax.inject.Inject

internal object SkieDirectoriesManager {

    const val baseTaskName = "createBuildDirectory"

    fun configureCreateSkieBuildDirectoryTask(linkTask: KotlinNativeLink) {
        linkTask.registerSkieLinkBasedTask<SkieCreateBuildDirectoryTask>(baseTaskName) {
            directory.set(linkTask.skieBuildDirectory)
        }
    }

    abstract class SkieCreateBuildDirectoryTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

        @get:Internal
        val directory: Property<SkieBuildDirectory> = objects.property(SkieBuildDirectory::class.java)

        init {
            outputs.dir(directory.map { it.directory })
        }

        @TaskAction
        fun runTask() {
            val directory = directory.get()

            directory.createDirectories()
            directory.resetTemporaryDirectories()
        }
    }
}

// TODO Decide if this should be public or internal

val KotlinNativeLink.skieDirectories: SkieDirectories
    get() = SkieDirectories(
        project.layout.buildDirectory.dir("skie/${binary.name}/${binary.target.targetName}").get().asFile,
    )

val KotlinNativeLink.skieBuildDirectory: SkieBuildDirectory
    get() = skieDirectories.buildDirectory

internal val KotlinNativeLink.createSkieBuildDirectoryTask: TaskProvider<Task>
    get() = project.tasks.named(skieLinkTaskName(SkieDirectoriesManager.baseTaskName))
