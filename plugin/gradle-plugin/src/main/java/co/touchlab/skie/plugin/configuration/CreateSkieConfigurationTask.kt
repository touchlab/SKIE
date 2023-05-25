package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.plugin.util.registerSkieLinkBasedTask
import co.touchlab.skie.plugin.util.skieBuildDirectory
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
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
        val serializedConfiguration = configuration.serialize()

        configurationFile.writeText(serializedConfiguration)
    }

    companion object {

        fun registerTask(linkTask: KotlinNativeLink) {
            val createConfiguration = linkTask.registerSkieLinkBasedTask<CreateSkieConfigurationTask>("createConfiguration") {
                configurationFile.set(linkTask.skieBuildDirectory.skieConfiguration)
            }

            linkTask.inputs.files(createConfiguration.map { it.outputs })
        }
    }
}
