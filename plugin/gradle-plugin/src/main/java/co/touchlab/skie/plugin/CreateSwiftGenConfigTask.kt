package co.touchlab.skie.plugin

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.gradle_plugin.BuildConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import java.io.File
import org.gradle.api.Project

abstract class CreateSwiftGenConfigTask : DefaultTask() {

    @get:OutputFile
    val configFile: File = project.layout.buildDirectory.get().asFile.resolve("${BuildConfig.KOTLIN_PLUGIN_ID}/config.json")

    init {
        doNotTrackState("Tracking configuration changes is currently not supported.")
    }

    @TaskAction
    fun createConfig() {
        configFile.parentFile.mkdirs()

        val configuration = createConfiguration(project)

        configFile.writeText(configuration.serialize())
    }

    companion object {

        const val name: String = "createSwiftGenConfig"

        fun createConfiguration(project: Project): Configuration {
            val extension = project.extensions.getByType<SkieExtension>()

            return extension.buildConfiguration()
        }
    }
}
