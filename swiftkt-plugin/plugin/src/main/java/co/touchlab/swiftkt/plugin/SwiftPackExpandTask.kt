package co.touchlab.swiftkt.plugin

import co.touchlab.swiftpack.plugin.SwiftNameProvider
import co.touchlab.swiftpack.plugin.SwiftPack.produceSwiftFile
import co.touchlab.swiftpack.plugin.SwiftPack.swiftPackModules
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property
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

    val swiftNameProvider: Property<SwiftNameProvider> = project.objects.property<SwiftNameProvider>().convention(SimpleSwiftNameProvider())

    @TaskAction
    fun expandTemplates() {
        val swiftNameProvider = swiftNameProvider.get()
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

