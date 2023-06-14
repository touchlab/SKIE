package co.touchlab.skie.plugin.api

import java.io.File

data class SwiftCompilerConfiguration(
    val sourceFilesDirectory: File,
    val swiftVersion: String,
    val additionalFlags: List<String>,
)
