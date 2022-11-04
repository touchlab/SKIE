package co.touchlab.skie.api.impl

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.SkieModule
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import java.io.File

class DefaultSkieContext(
    override val module: SkieModule,
    override val swiftSourceFiles: List<File>,
    override val expandedSwiftDir: File,
    override val frameworkLayout: FrameworkLayout,
    override val disableWildcardExport: Boolean,
) : SkieContext
