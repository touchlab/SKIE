package co.touchlab.skie.sir.compilation

import co.touchlab.skie.sir.element.SirCompilableFile
import co.touchlab.skie.util.directory.SkieBuildDirectory
import java.nio.file.Path
import kotlin.io.path.nameWithoutExtension

// Instantiate only in ObjectFileProvider
class ObjectFileProvider(
    private val skieBuildDirectory: SkieBuildDirectory,
) {

    private val compilableFilesCache = mutableMapOf<SirCompilableFile, ObjectFile>()

    private val additionalFilesCache = mutableMapOf<Path, ObjectFile>()

    val allObjectFiles: List<ObjectFile>
        get() = compilableFilesCache.values + additionalFilesCache.values

    fun getOrCreate(compilableFile: SirCompilableFile): ObjectFile =
        compilableFilesCache.getOrPut(compilableFile) {
            val objectFilePath = skieBuildDirectory.swiftCompiler.objectFiles.objectFile(compilableFile.absolutePath.nameWithoutExtension).toPath()

            ObjectFile(objectFilePath)
        }

    fun getOrCreate(path: Path): ObjectFile =
        additionalFilesCache.getOrPut(path.toAbsolutePath()) {
            ObjectFile(path.toAbsolutePath())
        }
}
