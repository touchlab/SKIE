package co.touchlab.skie.buildsetup.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import java.io.File

abstract class SkieRoot : Plugin<Project> {

    override fun apply(project: Project): Unit = with(project) {
        apply<DevRoot>()
        apply<SkieBase>()

        registerReplaceDataTask()
    }

    private fun Project.registerReplaceDataTask() {
        tasks.register("replaceData") {
            doLast {
                /**
                 * Format: OLD1|||NEW1&&&OLD2|||NEW2
                 */
                val replacementString = System.getenv()["SKIE_REPLACEMENT_STRING"] ?: return@doLast

                replaceData(replacementString)
            }
        }
    }
}

private fun Project.replaceData(replacementString: String) {
    val replacements = parseReplacementString(replacementString)

    projectDir.walkTopDown()
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
