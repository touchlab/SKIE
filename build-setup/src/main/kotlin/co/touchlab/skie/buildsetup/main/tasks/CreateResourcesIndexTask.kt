package co.touchlab.skie.buildsetup.main.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class CreateResourcesIndexTask : DefaultTask() {

    @get:Input
    abstract val baseResourcesPaths: ListProperty<String>

    @get:InputFiles
    abstract val resources: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    init {
        group = "other"
    }

    @TaskAction
    fun execute() {
        val baseResourcesFiles = baseResourcesPaths.get().map { File(it) }

        val indexContent = resources.files.joinToString("\n") { resourceName(baseResourcesFiles, it) }

        val outputFile = outputFile.get().asFile
        outputFile.parentFile.mkdirs()
        outputFile.writeText("$indexContent\n")
    }

    private fun resourceName(baseResourcesFiles: List<File>, file: File): String {
        val baseResourcesFolder = baseResourcesFiles.first { file.startsWith(it) }

        return file.relativeTo(baseResourcesFolder).path
    }
}
