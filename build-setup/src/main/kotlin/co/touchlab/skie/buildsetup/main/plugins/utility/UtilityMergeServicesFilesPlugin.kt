@file:Suppress("UnstableApiUsage")

package co.touchlab.skie.buildsetup.main.plugins.utility

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources

abstract class UtilityMergeServicesFilesPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        tasks.withType<ProcessResources>().configureEach {
            filesMatching("META-INF/services/**") {
                relativePath.parent?.let {
                    destinationDir.resolve(it.pathString).mkdirs()
                }

                val outputFile = destinationDir.resolve(relativePath.pathString)

                val outputFileContent = if (outputFile.exists()) {
                    val outputFileLines = file.readLines() + outputFile.readLines()

                    outputFileLines.joinToString(System.lineSeparator())
                } else {
                    file.readText()
                }

                outputFile.writeText(outputFileContent)

                exclude()
            }
        }
    }
}

