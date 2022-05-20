package co.touchlab.swikt.plugin

import io.outfoxx.swiftpoet.FileSpec
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.STRING
import io.outfoxx.swiftpoet.TypeSpec
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.intellij.lang.annotations.Language
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File
import javax.inject.Inject

abstract class SwiftGenerateTask
@Inject constructor(
    private val kotlinSourceSet: KotlinSourceSet
) : DefaultTask() {
    init {
        description = "Swift generation task"
        group = BasePlugin.BUILD_GROUP
    }

    @get:OutputDirectory
    abstract val generatedSourceDir: DirectoryProperty

    @TaskAction
    fun generateSwift() {
        // Uncomment this to generate a dummy file in common
//        if (kotlinSourceSet.name == "commonMain") {
//            generatedSourceDir.asFile.get().writeDummySwift()
//        }
    }
}

private fun File.writeDummySwift() {
    FileSpec.builder("GeneratedSwift")
        .addType(
            TypeSpec.classBuilder("GeneratedSwift")
                .addFunction(
                    FunctionSpec.builder("foo")
                        .returns(STRING)
                        .addCode("GeneratedKotlinKt.foo")
                        .build()
                )
                .build()
        )
        .build()
        .writeTo(this)

}

@Language("swift")
private fun swiftTemplate() = """
    class GeneratedSwift {
        func foo() -> String {
            GeneratedKotlinKt.foo
        }
    }
""".trimIndent()
