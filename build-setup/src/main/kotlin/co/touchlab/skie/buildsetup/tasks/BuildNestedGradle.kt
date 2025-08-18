package co.touchlab.skie.buildsetup.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.tooling.GradleConnector

// WIP Remove
abstract class BuildNestedGradle : DefaultTask() {

    @get:InputDirectory
    abstract val projectDir: DirectoryProperty

    @get:Input
    abstract val tasks: ListProperty<String>

    @TaskAction
    fun buildNestedGradle() {
        val connection = GradleConnector.newConnector()
            .forProjectDirectory(projectDir.get().asFile)
            .connect()

        connection.newBuild()
            .forTasks(*tasks.get().toTypedArray())
            .setStandardOutput(System.out)
            .setStandardError(System.err)
            .run()
    }
}
