package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.plugin.SwiftPack.swiftPackModules
import co.touchlab.swiftpack.plugin.SwiftNameProvider
import co.touchlab.swiftpack.plugin.SwiftPack.produceSwiftFile
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import javax.inject.Inject

abstract class SwiftPackExpandTask @Inject constructor(
    private val framework: Framework,
): DefaultTask() {

    init {
        group = "swiftkt"
        description = "Expands SwiftPack templates for ${framework.name}"
    }

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun expandTemplates() {
        val swiftNameProvider = object: SwiftNameProvider {
            override fun getSwiftName(kotlinClassName: String): String {
                return kotlinClassName.split(".").last()
            }
        }
        outputDir.asFile.get().deleteRecursively()
        val modules = framework.swiftPackModules.get()
        modules.forEach { (namespace, module) ->
            module.files.forEach { file ->
                val finalContents = file.produceSwiftFile(swiftNameProvider)
                val targetSwiftFile = outputDir.file("${namespace}_${module.name}_${file.name}.swift").get().asFile
                targetSwiftFile.writeText(finalContents)
            }
        }
    }
}
