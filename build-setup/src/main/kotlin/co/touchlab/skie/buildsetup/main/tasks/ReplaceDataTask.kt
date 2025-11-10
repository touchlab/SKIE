package co.touchlab.skie.buildsetup.main.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class ReplaceDataTask : DefaultTask() {

    /**
     * Format: OLD1|||NEW1&&&OLD2|||NEW2
     */
    @get:Input
    @get:Optional
    abstract val replacementString: Property<String>

    @get:OutputDirectory
    abstract val projectDirectory: DirectoryProperty

    init {
        group = "other"
    }

    @TaskAction
    fun execute() {
        val replacementString = replacementString.orNull ?: return

        replaceData(replacementString)
    }

    private fun replaceData(replacementString: String) {
        val replacements = parseReplacementString(replacementString)

        projectDirectory.get().asFile.walkTopDown()
            .filter { it.isFile && it.extension in listOf("kt", "kts", "properties", "json") }
            .forEach {
                it.replace(replacements)
            }
    }

    private fun File.replace(replacements: List<Replacement>) {
        val fileContent = readText()

        val newContent = replacements.fold(fileContent) { acc, replacement ->
            acc.replace(replacement.from, replacement.to)
        }

        if (newContent != fileContent) {
            writeText(newContent)
        }
    }

    private fun parseReplacementString(replacementString: String): List<Replacement> =
        replacementString
            .split("&&&")
            .map { Replacement(it.substringBefore("|||"), it.substringAfter("|||")) }

    private data class Replacement(val from: String, val to: String)
}
