package co.touchlab.skie.sir

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.sir.element.SirCompilableFile
import co.touchlab.skie.sir.element.SirIrFile
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirSourceFile
import co.touchlab.skie.util.cache.writeTextIfDifferent
import co.touchlab.skie.util.directory.SkieBuildDirectory
import java.nio.file.Path
import kotlin.io.path.absolute
import kotlin.io.path.extension

class SirFileProvider(
    private val skieModule: SirModule.Skie,
    private val kirProvider: KirProvider,
    private val skieBuildDirectory: SkieBuildDirectory,
) {

    private val irFileByPathCache = mutableMapOf<Path, SirIrFile>()

    private val writtenSourceFileByPathCache = mutableMapOf<Path, SirSourceFile>()

    private val generatedSourceFileByPathCache = mutableMapOf<Path, SirSourceFile>()

    private val skieNamespace: String
        get() = kirProvider.skieModule.name

    fun getWrittenSourceFileFromSkieNamespace(name: String): SirSourceFile {
        val path = relativePath(skieNamespace, name)

        check(path !in generatedSourceFileByPathCache) {
            "Generated source file for $path already exists. Combining written and generated source files is not supported."
        }

        return writtenSourceFileByPathCache.getOrPut(path) {
            SirSourceFile(skieModule, path)
        }
    }

    fun getGeneratedSourceFile(irFile: SirIrFile): SirSourceFile {
        check(irFile.relativePath !in writtenSourceFileByPathCache) {
            "Written source file for ${irFile.relativePath} already exists. Combining written and generated source files is not supported."
        }

        return generatedSourceFileByPathCache.getOrPut(irFile.relativePath) {
            SirSourceFile(irFile.module, irFile.relativePath, irFile)
        }
    }

    fun getIrFileFromSkieNamespace(name: String): SirIrFile =
        getIrFile(skieNamespace, name)

    fun getIrFile(namespace: String, name: String): SirIrFile =
        irFileByPathCache.getOrPut(relativePath(namespace, name)) {
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

    companion object {

        fun relativePath(namespace: String, name: String): Path =
            Path.of("$namespace/$namespace.$name.swift")
    }
}
