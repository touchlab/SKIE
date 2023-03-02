package co.touchlab.skie.plugin.api

import co.touchlab.skie.plugin.api.debug.DebugInfoDirectory
import co.touchlab.skie.plugin.api.debug.DumpSwiftApiPoint
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import java.io.File

interface SkieContext {

    val module: SkieModule
    val swiftSourceFiles: List<File>
    val expandedSwiftDir: File
    val debugInfoDirectory: DebugInfoDirectory

    val frameworkLayout: FrameworkLayout
    val disableWildcardExport: Boolean
    val dumpSwiftApiPoints: Set<DumpSwiftApiPoint>
}
