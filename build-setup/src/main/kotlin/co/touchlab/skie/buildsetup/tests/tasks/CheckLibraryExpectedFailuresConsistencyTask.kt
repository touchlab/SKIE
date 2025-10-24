package co.touchlab.skie.buildsetup.tests.tasks

import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersion
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class CheckLibraryExpectedFailuresConsistencyTask : DefaultTask() {

    @get:InputFiles
    abstract val expectedFailuresFiles: ConfigurableFileCollection

    @get:Input
    abstract val supportedKotlinVersions: SetProperty<SupportedKotlinVersion>

    init {
        group = "verification"
    }

    @TaskAction
    fun execute() {
        val errors = mutableListOf<String>()

        val supportedKotlinVersions = this@CheckLibraryExpectedFailuresConsistencyTask.supportedKotlinVersions.get()
        val parsedExpectedFailuresFiles = expectedFailuresFiles.files.map { parseExpectedFailuresFile(it) }

        validateSections(parsedExpectedFailuresFiles, errors)
        validateSupportedKotlinVersions(parsedExpectedFailuresFiles, supportedKotlinVersions, errors)

        if (errors.isNotEmpty()) {
            val fullError = errors.joinToString("\n\n")

            error(fullError)
        }
    }

    private fun parseExpectedFailuresFile(file: File): ExpectedFailuresFile {
        val content = file.readText()

        val sections = content
            .split("##")
            .filter { it.lines().firstOrNull()?.isNotBlank() ?: false }
            .map { fullContent ->
                val header = fullContent.lines().first().substringBefore("---").trim()

                val content = fullContent.lines().drop(1).joinToString("\n")

                ExpectedFailuresFile.Section(header, content)
            }
            .filter { it.name.isNotBlank() }

        return ExpectedFailuresFile(file.parentFile.name, sections)
    }

    private fun validateSections(expectedFailuresFiles: List<ExpectedFailuresFile>, errors: MutableList<String>) {
        val localErrors = mutableListOf<String>()

        expectedFailuresFiles.forEachIndexed { lhsIndex, lhs ->
            expectedFailuresFiles.forEachIndexed { rhsIndex, rhs ->
                if (lhsIndex < rhsIndex) {
                    val newErrors = findNamesOfDifferentSections(lhs, rhs)
                        .map { "Section '$it' of '${lhs.name}' and '${rhs.name}'" }

                    localErrors.addAll(newErrors)
                }
            }
        }

        if (localErrors.isNotEmpty()) {
            val fullError = "The following sections of expected failures files differ: \n" +
                localErrors.joinToString("\n") { "    $it" }

            errors.add(fullError)
        }
    }

    private fun findNamesOfDifferentSections(lhs: ExpectedFailuresFile, rhs: ExpectedFailuresFile): List<String> {
        val lhsSectionByName = lhs.sections.associateBy { it.name }
        val rhsSectionByName = rhs.sections.associateBy { it.name }

        val uniqueNames = lhs.sections.map { it.name }.toSet() + rhs.sections.map { it.name }.toSet()

        return uniqueNames.filter { sectionName ->
            val lhsSection = lhsSectionByName[sectionName]
            val rhsSection = rhsSectionByName[sectionName]

            lhsSection != null && rhsSection != null && lhsSection.content != rhsSection.content
        }
    }

    private fun validateSupportedKotlinVersions(
        expectedFailuresFiles: List<ExpectedFailuresFile>,
        supportedKotlinVersions: Set<SupportedKotlinVersion>,
        errors: MutableList<String>,
    ) {
        val supportedVersions = supportedKotlinVersions.map { it.name.toString() }

        val unsupportedVersions = expectedFailuresFiles.filter { it.name !in supportedVersions }

        if (unsupportedVersions.isNotEmpty()) {
            val fullError = "The following expected failures do not belong to any of the supported Kotlin versions.\n" +
                unsupportedVersions.joinToString("\n") { "    $it" }

            errors.add(fullError)
        }
    }

    private data class ExpectedFailuresFile(
        val name: String,
        val sections: List<Section>,
    ) {

        data class Section(val name: String, val content: String)
    }
}
