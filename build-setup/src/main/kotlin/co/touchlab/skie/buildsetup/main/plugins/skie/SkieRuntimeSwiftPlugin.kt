package co.touchlab.skie.buildsetup.main.plugins.skie

import co.touchlab.skie.buildsetup.main.plugins.base.BaseKotlinPlugin
import co.touchlab.skie.buildsetup.main.plugins.utility.UtilityMinimumTargetKotlinVersionPlugin
import co.touchlab.skie.buildsetup.main.tasks.CreateResourcesIndexTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.register
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper

abstract class SkieRuntimeSwiftPlugin : Plugin<Project> {

    override fun apply(target: Project): Unit = with(target) {
        apply<BaseKotlinPlugin>()
        apply<UtilityMinimumTargetKotlinVersionPlugin>()
        apply<KotlinPluginWrapper>()

        configureCreateResourcesIndexTask()
    }

    private fun Project.configureCreateResourcesIndexTask() {
        val createResourcesIndex = registerCreateResourcesIndexTask()

        tasks.named("processResources").configure {
            dependsOn(createResourcesIndex)
        }
    }

    private fun Project.registerCreateResourcesIndexTask(): TaskProvider<out Task> {
        val taskProvider = tasks.register<CreateResourcesIndexTask>("createResourcesIndex") {
        }

        extensions.configure<SourceSetContainer> {
            taskProvider.configure {
                val mainSourceSet = named("main")

                val baseResourcesPaths = mainSourceSet.map { sourceSet -> sourceSet.resources.srcDirs.map { it.absolutePath } }

                val resourcesProvider = mainSourceSet.map { it.resources.files }

                val outputFileProvider = mainSourceSet.map {
                    it.output.resourcesDir!!.resolve("co/touchlab/skie/runtime/index.txt")
                }

                this.baseResourcesPaths.set(baseResourcesPaths)

                resources.setFrom(resourcesProvider)

                outputFile.fileProvider(outputFileProvider)
            }
        }

        return taskProvider
    }
}
