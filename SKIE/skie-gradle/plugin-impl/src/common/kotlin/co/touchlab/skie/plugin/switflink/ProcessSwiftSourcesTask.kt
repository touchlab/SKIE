package co.touchlab.skie.plugin.switflink

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ProcessSwiftSourcesTask : DefaultTask() {

    @get:OutputDirectory
    abstract val output: Property<File>

    @TaskAction
    fun execute() {
        // WIP
        output.get().mkdirs()
    }
}
