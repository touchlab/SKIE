package co.touchlab.skie.plugin.api

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.analytics.producer.AnalyticsCollector
import co.touchlab.skie.plugin.api.debug.DebugInfoDirectory
import co.touchlab.skie.plugin.api.debug.DumpSwiftApiPoint
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.util.FrameworkLayout

interface SkieContext {

    val module: SkieModule
    val configuration: Configuration

    val swiftCompilerConfiguration: SwiftCompilerConfiguration

    val debugInfoDirectory: DebugInfoDirectory

    val frameworkLayout: FrameworkLayout
    val disableWildcardExport: Boolean
    val dumpSwiftApiPoints: Set<DumpSwiftApiPoint>

    val analyticsCollector: AnalyticsCollector
}
