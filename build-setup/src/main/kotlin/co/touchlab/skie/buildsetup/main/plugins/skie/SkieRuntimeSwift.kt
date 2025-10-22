package co.touchlab.skie.buildsetup.main.plugins.skie

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.apply
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import java.io.File

abstract class SkieRuntimeSwift : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<BaseKotlin>()
        apply<KotlinPluginWrapper>()

        configureCreateResourcesIndexTask()
    }

    private fun Project.configureCreateResourcesIndexTask() {
        val createResourcesIndex = registerCreateResourcesIndexTask()

        tasks.named("processResources").configure {
            dependsOn(createResourcesIndex)
        }
    }

    private fun Project.registerCreateResourcesIndexTask(): TaskProvider<Task> {
        val sourceSets = extensions.getByName("sourceSets") as SourceSetContainer

        return tasks.register("createResourcesIndex") {
            val mainSourceSet = sourceSets.named("main")

            val resourcesProvider = mainSourceSet.map { it.resources.files }

            val outputFileProvider = mainSourceSet.map {
                it.output.resourcesDir!!.resolve("co/touchlab/skie/runtime/index.txt")
            }

            inputs.files(resourcesProvider)
            outputs.file(outputFileProvider)

            doLast {
                val indexContent = resourcesProvider.get().joinToString("\n") { mainSourceSet.get().resourceName(it) }

                val outputFile = outputFileProvider.get()
                outputFile.parentFile.mkdirs()
                outputFile.writeText("$indexContent\n")
            }
        }
    }

    private fun SourceSet.resourceName(file: File): String {
        val baseResourcesPaths = this.resources.srcDirs

        val baseResourcesFolder = baseResourcesPaths.first { file.startsWith(it) }

        return file.relativeTo(baseResourcesFolder).path
    }
}
