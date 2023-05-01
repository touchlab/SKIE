package co.touchlab.skie.plugin.api

import java.io.File

data class SwiftCompilerConfiguration(
    val sourceFiles: List<File>,
    val expandedSourcesDir: File,
    val swiftVersion: String,
    val parallelCompilation: Boolean,
    val additionalFlags: List<String>,
)
