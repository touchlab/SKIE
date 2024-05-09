package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.plugin.SkieTarget
import co.touchlab.skie.plugin.directory.createSkieBuildDirectoryTask
import co.touchlab.skie.plugin.skieBuildDirectory
import co.touchlab.skie.plugin.util.registerSkieTargetBasedTask
import co.touchlab.skie.plugin.util.skieConfiguration
import groovy.json.JsonOutput
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class CreateSkieConfigurationTask : DefaultTask() {

    @get:Input
    abstract val configuration: Property<GradleSkieConfigurationData>

    @get:OutputFile
    abstract val configurationFile: Property<File>

    init {
        this.doNotTrackState("Tracking configuration changes is currently not supported.")
    }

    @TaskAction
    fun runTask() {
        val configurationFile = configurationFile.get()

        configurationFile.parentFile.mkdirs()

        val configuration = configuration.get()
        val serializedConfiguration = JsonOutput.toJson(configuration)
        val readableSerializedConfiguration = JsonOutput.prettyPrint(serializedConfiguration)

        configurationFile.writeText(readableSerializedConfiguration)
    }

    companion object {

        fun registerTask(target: SkieTarget) {
            val createConfiguration = target.registerSkieTargetBasedTask<CreateSkieConfigurationTask>("createConfiguration") {
                val skieExtension = project.skieExtension
                configurationFile.set(target.skieBuildDirectory.skieConfiguration)
                configuration.set(project.provider { skieExtension.buildConfiguration(target.outputKind) })

                dependsOn(target.createSkieBuildDirectoryTask)
            }

            target.task.configure {
                inputs.files(createConfiguration)
            }
        }
    }
}
