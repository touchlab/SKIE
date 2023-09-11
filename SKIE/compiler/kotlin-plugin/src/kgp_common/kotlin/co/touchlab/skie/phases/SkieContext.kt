package co.touchlab.skie.phases

import co.touchlab.skie.phases.analytics.performance.SkiePerformanceAnalytics
import co.touchlab.skie.plugin.analytics.AnalyticsCollector
import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.phases.swift.SwiftCompilerConfiguration
import co.touchlab.skie.util.FrameworkLayout
import co.touchlab.skie.util.Reporter
import co.touchlab.skie.util.directory.SkieBuildDirectory
import co.touchlab.skie.util.directory.SkieDirectories

interface SkieContext {

    val module: SkieModule

    val skieConfiguration: SkieConfiguration

    val swiftCompilerConfiguration: SwiftCompilerConfiguration

    val skieDirectories: SkieDirectories

    val frameworkLayout: FrameworkLayout

    val analyticsCollector: AnalyticsCollector

    val skiePerformanceAnalyticsProducer: SkiePerformanceAnalytics.Producer

    val reporter: Reporter
}

val SkieContext.skieBuildDirectory: SkieBuildDirectory
    get() = skieDirectories.buildDirectory
