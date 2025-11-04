@file:Suppress("UnstableApiUsage")

package co.touchlab.skie.buildsetup.main.plugins.utility

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.AbstractCopyTask
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.withType
import org.gradle.language.jvm.tasks.ProcessResources

abstract class UtilityMergeServicesFilesPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        tasks.withType<ProcessResources>().configureEach {
            configureServicesMerging()
        }

        tasks.withType<Jar>().configureEach {
            configureServicesMerging()
        }
    }

    private fun AbstractCopyTask.configureServicesMerging() {
        val mergedFilesContentPerFile = mutableMapOf<String, MutableList<String>>()

        filesMatching("META-INF/services/**") {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE

            val mergedFilesContent = mergedFilesContentPerFile.getOrPut(relativePath.pathString) { mutableListOf() }

            var isFirstLine = true

            filter {
                mergedFilesContent.add(it)
                if (isFirstLine) {
                    isFirstLine = false
                    mergedFilesContent.joinToString(System.lineSeparator())
                } else {
                    it
                }
            }
        }
    }
}

