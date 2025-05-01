package co.touchlab.skie.plugin.directory

import co.touchlab.skie.plugin.SkieTarget
import co.touchlab.skie.plugin.util.registerSkieTargetBasedTask
import co.touchlab.skie.plugin.util.skieTargetBasedTaskName
import co.touchlab.skie.util.directory.SkieDirectories
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider

object SkieDirectoriesManager {

    const val BASE_TASK_NAME: String = "createSkieDirectories"

    fun configureCreateSkieBuildDirectoryTask(target: SkieTarget) {
        target.registerSkieTargetBasedTask<SkieCreateSkieDirectoriesTask>(BASE_TASK_NAME) {
            skieDirectories.set(target.skieDirectories)
        }
    }

    abstract class SkieCreateSkieDirectoriesTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

        @get:Internal
        val skieDirectories: Property<SkieDirectories> = objects.property(SkieDirectories::class.java)

        init {
            outputs.dirs(skieDirectories.map { it.directories })
        }

        @TaskAction
        fun runTask() {
            skieDirectories.get().run {
                createDirectories()
                resetTemporaryDirectories()
            }
        }
    }
}

val SkieTarget.createSkieBuildDirectoryTask: TaskProvider<Task>
    get() = project.tasks.named(skieTargetBasedTaskName(SkieDirectoriesManager.BASE_TASK_NAME))
