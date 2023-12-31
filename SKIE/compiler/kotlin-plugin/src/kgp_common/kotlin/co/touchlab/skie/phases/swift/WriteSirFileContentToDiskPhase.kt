package co.touchlab.skie.phases.swift

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.util.cache.writeTextIfDifferent
import co.touchlab.skie.util.directory.SkieBuildDirectory
import java.io.File
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.pathString

object WriteSirFileContentToDiskPhase : SirPhase {

    context(SirPhase.Context)
    override fun execute() {
        val cacheAwareFileGenerator = CacheAwareFileGenerator(skieBuildDirectory.swift.generated)

        with(cacheAwareFileGenerator) {
            sirProvider.files
                .groupBy { it.relativePath.absolutePathString().lowercase() }
                .values
                .forEach { fileGroup ->
                    val sourceCode = fileGroup.joinToString(separator = "\n\n") { it.content }

                    writeFile(fileGroup.first().relativePath, sourceCode)
                }
        }

        cacheAwareFileGenerator.deleteOldFiles()
    }

    private class CacheAwareFileGenerator(
        private val generatedSwiftDirectory: SkieBuildDirectory.Swift.Generated,
    ) {

        private val generatedFiles = mutableSetOf<File>()

        fun writeFile(relativePath: Path, content: String) {
            val file = generatedSwiftDirectory.directory.resolve(relativePath.pathString)

            file.parentFile.mkdirs()

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
