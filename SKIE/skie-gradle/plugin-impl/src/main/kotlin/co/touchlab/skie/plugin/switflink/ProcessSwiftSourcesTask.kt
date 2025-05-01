package co.touchlab.skie.plugin.switflink

import java.io.File
import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class ProcessSwiftSourcesTask : DefaultTask() {

    @get:OutputDirectory
    abstract val output: Property<File>

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun execute() {
        syncFiles()

        verifyFileNames()
    }

    private fun syncFiles() {
        fileSystemOperations.sync {
            duplicatesStrategy = DuplicatesStrategy.FAIL

            from(inputs.files) {
                include("**/*.swift")
            }

            into(output)
        }
    }

    private fun verifyFileNames() {
        output.get().walkTopDown()
            .filter { it.extension == "swift" }
            .groupBy { it.name }
            .forEach { (name, files) ->
                if (files.size > 1) {
                    throw IllegalStateException(
                        "Files $files have the same name '$name'. " +
                            "This is not allowed in Swift where each file in a given module must have a unique name regardless its path.",
                    )
                }
            }
    }
}
