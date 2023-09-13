package co.touchlab.skie.phases.swift

import java.io.File

// TODO Unify with SkieConfiguration
data class SwiftCompilerConfiguration(
    val sourceFilesDirectory: File,
    val swiftVersion: String,
    val additionalFlags: List<String>,
)
