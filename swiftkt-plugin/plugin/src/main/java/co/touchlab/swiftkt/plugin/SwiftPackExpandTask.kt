package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.spi.produceSwiftFile
import co.touchlab.swiftpack.plugin.SwiftPack.swiftPackModules
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import javax.inject.Inject

abstract class SwiftPackExpandTask @Inject constructor(
    objectFactory: ObjectFactory,
    layout: ProjectLayout,
    private val framework: Framework,
): DefaultTask() {

    init {
        group = "swiftkt"
        description = "Expands SwiftPack templates for ${framework.name}"
    }

    @get:OutputDirectory
    val outputDir: DirectoryProperty = objectFactory.directoryProperty()
        .convention(layout.buildDirectory.dir("generated/swiftpack-expanded/${framework.name}/${framework.target.targetName}"))

    @TaskAction
    fun expandTemplates() {
        val swiftNameProvider = SimpleSwiftNameProvider()
        outputDir.asFile.get().apply {
            deleteRecursively()
            mkdirs()
        }
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

