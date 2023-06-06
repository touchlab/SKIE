package co.touchlab.skie.plugin.api

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.analytics.producer.AnalyticsCollector
import co.touchlab.skie.plugin.api.analytics.SkiePerformanceAnalyticsProducer
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import co.touchlab.skie.plugin.license.SkieLicense
import co.touchlab.skie.util.directory.SkieBuildDirectory
import co.touchlab.skie.util.directory.SkieDirectories

interface SkieContext {

    val module: SkieModule

    val license: SkieLicense

    val configuration: Configuration

    val swiftCompilerConfiguration: SwiftCompilerConfiguration

    val skieDirectories: SkieDirectories

    val frameworkLayout: FrameworkLayout

    val analyticsCollector: AnalyticsCollector

    val skiePerformanceAnalyticsProducer: SkiePerformanceAnalyticsProducer
}

val SkieContext.skieBuildDirectory: SkieBuildDirectory
    get() = skieDirectories.buildDirectory
