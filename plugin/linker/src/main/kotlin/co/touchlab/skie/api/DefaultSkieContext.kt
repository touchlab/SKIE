package co.touchlab.skie.api

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.analytics.producer.AnalyticsCollector
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.SwiftCompilerConfiguration
import co.touchlab.skie.plugin.api.analytics.SkiePerformanceAnalyticsProducer
import co.touchlab.skie.plugin.api.debug.DebugInfoDirectory
import co.touchlab.skie.plugin.api.debug.DumpSwiftApiPoint
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.util.FrameworkLayout

class DefaultSkieContext(
    override val module: SkieModule,
    override val configuration: Configuration,
    override val swiftCompilerConfiguration: SwiftCompilerConfiguration,
    override val debugInfoDirectory: DebugInfoDirectory,
    override val frameworkLayout: FrameworkLayout,
    override val disableWildcardExport: Boolean,
    override val dumpSwiftApiPoints: Set<DumpSwiftApiPoint>,
    override val analyticsCollector: AnalyticsCollector,
    override val skiePerformanceAnalyticsProducer: SkiePerformanceAnalyticsProducer,
) : SkieContext
