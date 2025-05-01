package co.touchlab.skie.phases.swift

import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.sir.element.SirSourceFile
import co.touchlab.skie.util.directory.SkieBuildDirectory
import co.touchlab.skie.util.file.deleteEmptyDirectoriesRecursively
import java.io.File

object ConvertSirSourceFilesToCompilableFilesPhase : SirPhase {

    context(SirPhase.Context)
    override suspend fun execute() {
        val cacheAwareFileGenerator = CacheAwareFileGenerator(skieBuildDirectory.swift.generated)

        with(cacheAwareFileGenerator) {
            sirProvider.skieModuleFiles
                .filterIsInstance<SirSourceFile>()
                .forEach {
                    it.convertToCompilableFile()
                }
        }

        cacheAwareFileGenerator.deleteOldFiles()
    }

    private class CacheAwareFileGenerator(private val generatedSwiftDirectory: SkieBuildDirectory.Swift.Generated) {

        private val generatedFiles = mutableSetOf<File>()

        context(SirPhase.Context)
        fun SirSourceFile.convertToCompilableFile() {
            val compilableFile = sirFileProvider.createCompilableFile(this)

            generatedFiles.add(compilableFile.absolutePath.toFile())
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
            generatedSwiftDirectory.directory.deleteEmptyDirectoriesRecursively()
        }
    }
}
