package co.touchlab.skie.phases

import co.touchlab.skie.phases.analytics.performance.SkiePerformanceAnalytics
import co.touchlab.skie.plugin.analytics.AnalyticsCollector
import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.phases.swift.SwiftCompilerConfiguration
import co.touchlab.skie.util.FrameworkLayout
import co.touchlab.skie.util.Reporter
import co.touchlab.skie.util.directory.SkieDirectories

class DefaultSkieContext(
    override val module: SkieModule,
    override val skieConfiguration: SkieConfiguration,
    override val swiftCompilerConfiguration: SwiftCompilerConfiguration,
    override val skieDirectories: SkieDirectories,
    override val frameworkLayout: FrameworkLayout,
    override val analyticsCollector: AnalyticsCollector,
    override val skiePerformanceAnalyticsProducer: SkiePerformanceAnalytics.Producer,
    override val reporter: Reporter,
) : SkieContext
