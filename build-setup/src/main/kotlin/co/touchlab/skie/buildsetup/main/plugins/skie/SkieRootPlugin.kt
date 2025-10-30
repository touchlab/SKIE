package co.touchlab.skie.buildsetup.main.plugins.skie

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlinPlugin
import co.touchlab.skie.buildsetup.main.plugins.base.BaseRootPlugin
import co.touchlab.skie.buildsetup.main.tasks.GenerateAllVersionsSmokeTestsCIActionsTask
import co.touchlab.skie.buildsetup.main.tasks.GenerateVersionedSmokeTestsCIActionsTask
import co.touchlab.skie.buildsetup.util.version.SupportedKotlinVersionProvider
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import java.io.File

abstract class SkieRootPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<BaseRootPlugin>()
        apply<BaseKotlinPlugin>()

        registerReplaceDataTask()
        registerGenerateCIActionsTasks()
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

    private fun Project.registerGenerateCIActionsTasks() {
        val generateAllVersionsSmokeTestsCIActions = tasks.register<GenerateAllVersionsSmokeTestsCIActionsTask>("generateAllVersionsSmokeTestsCIActions") {
            supportedVersions.set(SupportedKotlinVersionProvider.getSupportedKotlinVersions(project))
            outputPath.set(rootDir.parentFile.resolve(".github/workflows/smoke-tests-all-versions.yml"))
        }

        val generateVersionedSmokeTestsCIActions = tasks.register<GenerateVersionedSmokeTestsCIActionsTask>("generateVersionedSmokeTestsCIActions") {
            supportedVersions.set(SupportedKotlinVersionProvider.getSupportedKotlinVersions(project))
            outputDirectory.set(rootDir.parentFile.resolve(".github/workflows"))
        }

        tasks.register("generateCIActions") {
            dependsOn(generateAllVersionsSmokeTestsCIActions)
            dependsOn(generateVersionedSmokeTestsCIActions)
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
