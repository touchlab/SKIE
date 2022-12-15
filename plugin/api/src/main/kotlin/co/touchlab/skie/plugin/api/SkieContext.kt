package co.touchlab.skie.plugin.api

import co.touchlab.skie.plugin.api.util.FrameworkLayout
import java.io.File

interface SkieContext {

    val module: SkieModule
    val swiftSourceFiles: List<File>
    val expandedSwiftDir: File

    val frameworkLayout: FrameworkLayout
    val disableWildcardExport: Boolean
}
