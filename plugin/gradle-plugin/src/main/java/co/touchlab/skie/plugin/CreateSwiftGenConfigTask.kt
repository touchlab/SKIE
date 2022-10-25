package co.touchlab.skie.plugin

import co.touchlab.skie.gradle_plugin.BuildConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.getByType
import java.io.File

abstract class CreateSwiftGenConfigTask : DefaultTask() {

    @get:OutputFile
    val configFile: File = project.layout.buildDirectory.get().asFile.resolve("${BuildConfig.KOTLIN_PLUGIN_ID}/config.json")

    @TaskAction
    fun createConfig() {
        configFile.parentFile.mkdirs()

        val extension = project.extensions.getByType<SkieExtension>()

        val configuration = extension.buildConfiguration()

        configFile.writeText(configuration.serialize())
    }

    companion object {

        const val name: String = "createSwiftGenConfig"
    }
}
