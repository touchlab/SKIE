package co.touchlab.swikt.plugin

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.PropertySpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import javax.inject.Inject

abstract class KotlinGenerateTask
@Inject constructor(
    private val kotlinSourceSet: KotlinSourceSet
) : DefaultTask() {
    init {
        description = "Kotlin generation task"
        group = BasePlugin.BUILD_GROUP
    }

    @get:OutputDirectory
    abstract val generatedSourceDir: DirectoryProperty

    @TaskAction
    fun generateKotlin() {
        // Uncomment this to generate a dummy file in common
//        if (kotlinSourceSet.name == "commonMain") {
//            generatedSourceDir.asFile.get().writeDummyKotlin()
//        }
    }
}

private fun File.writeDummyKotlin() {
    FileSpec.builder("com.example.kotlin", "GeneratedKotlin")
        .addProperty(
            PropertySpec.builder("foo", String::class)
                .initializer("\"Bar\"")
                .build()
        )
        .build()
        .writeTo(this)
}

