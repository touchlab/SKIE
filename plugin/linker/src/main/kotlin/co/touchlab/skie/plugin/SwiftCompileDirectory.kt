package co.touchlab.skie.plugin

import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.library.impl.javaFile

class SwiftCompileDirectory(
    private val moduleName: String,
    private val root: File,
) {
    val workingDirectory = root.javaFile()
        get() {
            field.mkdirs()
            return field
        }

    val swiftModule = workingDirectory.resolve("${moduleName}.swiftmodule")
    val swiftInterface = workingDirectory.resolve("${moduleName}.swiftinterface")
    val privateSwiftInterface = workingDirectory.resolve("${moduleName}.private.swiftinterface")
    val swiftHeader = workingDirectory.resolve("${moduleName}-Swift.h")

    fun objectFiles(): List<String> = root.listFilesOrEmpty.filter { it.extension == "o" }.map { it.absolutePath }

}
