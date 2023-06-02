package co.touchlab.skie.plugin.api

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.analytics.producer.AnalyticsCollector
import co.touchlab.skie.plugin.api.analytics.SkiePerformanceAnalyticsProducer
import co.touchlab.skie.plugin.api.debug.DebugInfoDirectory
import co.touchlab.skie.plugin.api.debug.DumpSwiftApiPoint
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import java.io.File

interface SkieContext {

    val module: SkieModule
    val configuration: Configuration

    val swiftCompilerConfiguration: SwiftCompilerConfiguration

    val debugInfoDirectory: DebugInfoDirectory
    val skieBuildDirectory: File

    val frameworkLayout: FrameworkLayout
    val disableWildcardExport: Boolean
    val dumpSwiftApiPoints: Set<DumpSwiftApiPoint>

    val analyticsCollector: AnalyticsCollector
    val skiePerformanceAnalyticsProducer: SkiePerformanceAnalyticsProducer
}
