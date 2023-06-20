package co.touchlab.skie.api.phases

import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.api.model.DefaultSwiftModelScope
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.skieBuildDirectory
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import co.touchlab.skie.util.cache.writeTextIfDifferent
import co.touchlab.skie.util.directory.SkieBuildDirectory
import java.io.File
import kotlin.io.path.listDirectoryEntries

class GenerateSwiftCodePhase(
    private val skieContext: SkieContext,
    private val skieModule: DefaultSkieModule,
    private val swiftModelScope: DefaultSwiftModelScope,
    private val framework: FrameworkLayout,
) : SkieLinkingPhase {

    override fun execute() {
        val cacheAwareFileGenerator = CacheAwareFileGenerator(skieContext.skieBuildDirectory.swift.generated)

        with(cacheAwareFileGenerator) {
            generateSwiftPoetFiles()
            generateTextFiles()
        }

        cacheAwareFileGenerator.deleteOldFiles()
    }

    context(CacheAwareFileGenerator)
    private fun generateSwiftPoetFiles() {
        skieModule.produceSwiftPoetFiles(swiftModelScope, framework.moduleName)
            .forEach { fileSpec ->
                generateFile(fileSpec.name, fileSpec.toString())
            }
    }

    context(CacheAwareFileGenerator)
    private fun generateTextFiles() {
        skieModule.produceTextFiles()
            .forEach { textFile ->
                generateFile(textFile.name, textFile.content)
            }
    }

    private class CacheAwareFileGenerator(
        private val generatedSwiftDirectory: SkieBuildDirectory.Swift.Generated,
    ) {

        private val generatedFiles = mutableSetOf<File>()

        fun generateFile(name: String, content: String) {
            val file = generatedSwiftDirectory.swiftFile(name)

            file.writeTextIfDifferent(content)

            generatedFiles.add(file)
        }

        fun deleteOldFiles() {
            deleteNonGeneratedFiles()
            deleteEmptyDirectories()
        }

        private fun deleteNonGeneratedFiles() {
            generatedSwiftDirectory.directory
                .walkTopDown()
                .filter { it.isFile }
                .filterNot { generatedFiles.contains(it) }
                .forEach { it.delete() }
        }

        private fun deleteEmptyDirectories() {
            generatedSwiftDirectory.directory
                .walkTopDown()
                .filter { it.isDirectory }
                .filter { it.toPath().listDirectoryEntries().isEmpty() }
                .forEach { it.deleteRecursively() }
        }
    }
}
