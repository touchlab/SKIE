package co.touchlab.skie.phases

import co.touchlab.skie.configuration.RootConfiguration
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.provider.CompilerSkieConfigurationData
import co.touchlab.skie.phases.analytics.performance.SkiePerformanceAnalytics
import co.touchlab.skie.plugin.analytics.AnalyticsCollector
import co.touchlab.skie.util.FrameworkLayout
import co.touchlab.skie.util.Reporter
import co.touchlab.skie.util.directory.SkieBuildDirectory
import co.touchlab.skie.util.directory.SkieDirectories

interface CommonSkieContext {

    val context: CommonSkieContext

    val skiePhaseScheduler: SkiePhaseScheduler

    val skieConfigurationData: CompilerSkieConfigurationData

    val rootConfiguration: RootConfiguration

    val skieDirectories: SkieDirectories

    val framework: FrameworkLayout

    val analyticsCollector: AnalyticsCollector

    val skiePerformanceAnalyticsProducer: SkiePerformanceAnalytics.Producer

    val reporter: Reporter

    val skieBuildDirectory: SkieBuildDirectory
        get() = skieDirectories.buildDirectory

    val SkieConfigurationFlag.isEnabled: Boolean
        get() = rootConfiguration.isFlagEnabled(this)

    val SkieConfigurationFlag.isDisabled: Boolean
        get() = this.isEnabled.not()
}
