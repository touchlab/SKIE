package co.touchlab.skie.util

import co.touchlab.skie.util.directory.SkieDirectories

val SkieDirectories.objectFilePaths: List<String>
    get() = buildDirectory.swiftCompiler.objectFiles.allObjectFiles.map { it.absolutePath }
