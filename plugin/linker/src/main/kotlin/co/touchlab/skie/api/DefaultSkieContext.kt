package co.touchlab.skie.api

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.analytics.producer.AnalyticsCollector
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.SwiftCompilerConfiguration
import co.touchlab.skie.plugin.api.analytics.SkiePerformanceAnalyticsProducer
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import java.io.File
import co.touchlab.skie.plugin.license.SkieLicense
import co.touchlab.skie.util.directory.SkieDirectories

class DefaultSkieContext(
    override val module: SkieModule,
    override val license: SkieLicense,
    override val configuration: Configuration,
    override val swiftCompilerConfiguration: SwiftCompilerConfiguration,
    override val oldSkieBuildDirectory: File,
    override val skieDirectories: SkieDirectories,
    override val frameworkLayout: FrameworkLayout,
    override val analyticsCollector: AnalyticsCollector,
    override val skiePerformanceAnalyticsProducer: SkiePerformanceAnalyticsProducer,
) : SkieContext
