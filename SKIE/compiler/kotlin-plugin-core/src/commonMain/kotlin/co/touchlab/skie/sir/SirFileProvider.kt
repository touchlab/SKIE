package co.touchlab.skie.sir

import co.touchlab.skie.sir.element.SirCompilableFile
import co.touchlab.skie.sir.element.SirIrFile
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirSourceFile
import co.touchlab.skie.util.cache.writeTextIfDifferent
import co.touchlab.skie.util.directory.SkieBuildDirectory
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.absolutePathString
import kotlin.io.path.extension

class SirFileProvider(
    private val skieModule: SirModule.Skie,
    private val skieBuildDirectory: SkieBuildDirectory,
) {

    private val irFileByPathCache = mutableMapOf<String, SirIrFile>()

    private val writtenSourceFileByPathCache = mutableMapOf<String, SirSourceFile>()

    private val generatedSourceFileByPathCache = mutableMapOf<String, SirSourceFile>()

    fun getWrittenSourceFile(namespace: String, name: String): SirSourceFile {
        val path = relativePath(namespace, name)

        check(path.asCacheKey !in generatedSourceFileByPathCache) {
            "Generated source file for $path already exists. Combining written and generated source files is not supported."
        }

        return writtenSourceFileByPathCache.getOrPut(path.asCacheKey) {
            SirSourceFile(skieModule, path)
        }
    }

    fun getGeneratedSourceFile(irFile: SirIrFile): SirSourceFile {
        check(irFile.relativePath.asCacheKey !in writtenSourceFileByPathCache) {
            "Written source file for ${irFile.relativePath} already exists. Combining written and generated source files is not supported."
        }

        return generatedSourceFileByPathCache.getOrPut(irFile.relativePath.asCacheKey) {
            SirSourceFile(irFile.module, irFile.relativePath, irFile)
        }
    }

    fun getIrFile(namespace: String, name: String): SirIrFile =
        irFileByPathCache.getOrPut(relativePath(namespace, name).asCacheKey) {
            SirIrFile(namespace, name, skieModule)
        }

    fun createCompilableFile(sourceFile: SirSourceFile): SirCompilableFile {
        val absolutePath = skieBuildDirectory.swift.generated.path.resolve(sourceFile.relativePath)

        absolutePath.parent.toFile().mkdirs()

        absolutePath.writeTextIfDifferent(sourceFile.content)

        return SirCompilableFile(sourceFile.module, absolutePath, sourceFile)
    }

    fun loadCompilableFile(path: Path): SirCompilableFile {
        val absolutePath = path.absolute()

        check(absolutePath.startsWith(skieBuildDirectory.swift.path)) {
            "Custom source file must be located in the swift directory. Was: $absolutePath."
        }

        check(!absolutePath.startsWith(skieBuildDirectory.swift.generated.path)) {
            "Custom source file must not be located in the generated directory. Was: $absolutePath."
        }

        check(absolutePath.extension == "swift") {
            "Custom source file must have the swift extension. Was: $absolutePath."
        }

        return SirCompilableFile(skieModule, absolutePath, null)
    }

    private val Path.asCacheKey: String
        get() = this.normalize().absolutePathString().lowercase()

    companion object {

        fun relativePath(namespace: String, name: String): Path =
            Path.of("$namespace/$namespace.$name.swift")
    }
}
