package co.touchlab.skie.api

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.analytics.producer.AnalyticsCollector
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.debug.DebugInfoDirectory
import co.touchlab.skie.plugin.api.debug.DumpSwiftApiPoint
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import java.io.File

class DefaultSkieContext(
    override val module: SkieModule,
    override val configuration: Configuration,
    override val swiftSourceFiles: List<File>,
    override val expandedSwiftDir: File,
    override val debugInfoDirectory: DebugInfoDirectory,
    override val frameworkLayout: FrameworkLayout,
    override val disableWildcardExport: Boolean,
    override val dumpSwiftApiPoints: Set<DumpSwiftApiPoint>,
    override val analyticsCollector: AnalyticsCollector,
) : SkieContext
