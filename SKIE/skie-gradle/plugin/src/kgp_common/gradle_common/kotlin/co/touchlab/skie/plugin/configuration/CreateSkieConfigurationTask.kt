package co.touchlab.skie.plugin.configuration

import co.touchlab.skie.plugin.analytics.GradleAnalyticsManager
import co.touchlab.skie.plugin.directory.createSkieBuildDirectoryTask
import co.touchlab.skie.plugin.util.BaseSkieTask
import co.touchlab.skie.plugin.util.registerSkieLinkBasedTask
import co.touchlab.skie.plugin.directory.skieBuildDirectory
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputFile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeLink
import java.io.File

internal abstract class CreateSkieConfigurationTask : BaseSkieTask() {

    @get:OutputFile
    abstract val configurationFile: Property<File>

    init {
        doNotTrackState("Tracking configuration changes is currently not supported.")
    }

    override fun runTask() {
        val configurationFile = configurationFile.get()

        configurationFile.parentFile.mkdirs()

        val configuration = with(SkieExtension) { project.skieExtension.buildConfiguration() }
        val serializedConfiguration = configuration.serialize()

        configurationFile.writeText(serializedConfiguration)
    }

    companion object {

        fun registerTask(linkTask: KotlinNativeLink, analyticsManager: GradleAnalyticsManager) {
            val createConfiguration = linkTask.registerSkieLinkBasedTask<CreateSkieConfigurationTask>("createConfiguration", analyticsManager) {
                configurationFile.set(linkTask.skieBuildDirectory.skieConfiguration)

                dependsOn(linkTask.createSkieBuildDirectoryTask)
            }

            linkTask.inputs.files(createConfiguration.map { it.outputs })
        }
    }
}
