package co.touchlab.skie.sir

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.sir.element.SirCompilableFile
import co.touchlab.skie.sir.element.SirIrFile
import co.touchlab.skie.sir.element.SirModule
import co.touchlab.skie.sir.element.SirSourceFile
import java.nio.file.Path

class SirFileProvider(
    private val skieModule: SirModule.Skie,
    private val kirProvider: KirProvider,
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

    fun createCompilableFile(sourceFile: SirSourceFile, absoluteFile: Path): SirCompilableFile =
        SirCompilableFile(sourceFile, absoluteFile)

    companion object {

        fun relativePath(namespace: String, name: String): Path =
            Path.of("$namespace/$namespace.$name.swift")
    }
}
