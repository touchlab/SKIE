package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.plugin.util.SkieTarget
import co.touchlab.skie.plugin.configuration.SkieExtension.Companion.buildConfiguration
import co.touchlab.skie.plugin.directory.createSkieBuildDirectoryTask
import co.touchlab.skie.plugin.util.registerSkieTargetBasedTask
import co.touchlab.skie.plugin.util.skieBuildDirectory
import co.touchlab.skie.plugin.util.skieConfiguration
import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

internal abstract class CreateSkieConfigurationTask : DefaultTask() {

    @get:OutputFile
    abstract val configurationFile: Property<File>

    init {
        doNotTrackState("Tracking configuration changes is currently not supported.")
    }

    @TaskAction
    fun runTask() {
        val configurationFile = configurationFile.get()

        configurationFile.parentFile.mkdirs()

        val configuration = project.skieExtension.buildConfiguration()
        val serializedConfiguration = JsonOutput.toJson(configuration)

        configurationFile.writeText(serializedConfiguration)
    }

    companion object {

        fun registerTask(target: SkieTarget) {
            val createConfiguration = target.registerSkieTargetBasedTask<CreateSkieConfigurationTask>("createConfiguration") {
                configurationFile.set(target.skieBuildDirectory.skieConfiguration)

                dependsOn(target.createSkieBuildDirectoryTask)
            }

            target.task.configure {
                inputs.files(createConfiguration.map { it.outputs })
            }
        }
    }
}
