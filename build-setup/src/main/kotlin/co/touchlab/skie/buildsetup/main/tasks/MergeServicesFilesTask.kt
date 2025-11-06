package co.touchlab.skie.buildsetup.main.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class MergeServicesFilesTask : DefaultTask() {

    @get:InputFiles
    abstract val inputDirectories: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val output: DirectoryProperty

    init {
        group = "other"
    }

    @TaskAction
    fun execute() {
        val output = output.get().asFile
        output.deleteRecursively()
        output.mkdirs()

        val serviceFilesWithContent = getServiceFilesWithContent()

        writeServiceFiles(serviceFilesWithContent, output)
    }

    private fun getServiceFilesWithContent(): Map<String, List<String>> {
        val serviceFilesWithContent = mutableMapOf<String, MutableList<String>>()

        inputDirectories.forEach {
            serviceFilesWithContent.addAllServicesFiles(it)
        }

        return serviceFilesWithContent
    }

    private fun MutableMap<String, MutableList<String>>.addAllServicesFiles(directory: File) {
        directory.resolve("META-INF/services")
            .takeIf { it.exists() }
            ?.walkTopDown()
            ?.filter { it.isFile }
            ?.forEach { file ->
                val list = this.getOrPut(file.name) { mutableListOf() }

                val fileLines = file.readLines()

                list.addAll(fileLines)
            }
    }

    private fun writeServiceFiles(serviceFilesWithContent: Map<String, List<String>>, output: File) {
        serviceFilesWithContent.forEach { (name, lines) ->
            val content = lines.joinToString("\n")

            output.resolve(name).writeText(content)
        }
    }
}
