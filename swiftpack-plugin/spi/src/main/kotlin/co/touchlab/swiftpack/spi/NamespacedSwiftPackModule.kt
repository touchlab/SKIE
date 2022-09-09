package co.touchlab.swiftpack.spi

import co.touchlab.swiftpack.spec.SwiftPackModule
import java.io.File

data class NamespacedSwiftPackModule(
    val namespace: String,
    val module: SwiftPackModule,
) {
    data class Reference(
        val namespace: String,
        val moduleFile: File,
    )

    companion object {
        fun isSwiftPackModule(file: File): Boolean = file.isFile && file.extension == "swiftpack"

        fun moduleReferencesInDir(namespace: String, dir: File): List<Reference> {
            return dir.listFiles(::isSwiftPackModule)?.map { file ->
                Reference(namespace, file)
            } ?: emptyList()
        }
    }
}
