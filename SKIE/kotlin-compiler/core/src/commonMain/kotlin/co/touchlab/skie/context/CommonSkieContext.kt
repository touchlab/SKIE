package co.touchlab.skie.context

import co.touchlab.skie.analytics.performance.SkiePerformanceAnalytics
import co.touchlab.skie.configuration.GlobalConfiguration
import co.touchlab.skie.configuration.SkieConfigurationFlag
import co.touchlab.skie.configuration.provider.CompilerSkieConfigurationData
import co.touchlab.skie.phases.SkiePhaseScheduler
import co.touchlab.skie.plugin.analytics.AnalyticsCollector
import co.touchlab.skie.util.CompilerShim
import co.touchlab.skie.util.directory.FrameworkLayout
import co.touchlab.skie.util.KirReporter
import co.touchlab.skie.util.directory.SkieBuildDirectory
import co.touchlab.skie.util.directory.SkieDirectories

interface CommonSkieContext {

    val context: CommonSkieContext

    val skieConfigurationData: CompilerSkieConfigurationData

    val globalConfiguration: GlobalConfiguration

    val skieDirectories: SkieDirectories

    val framework: FrameworkLayout

    val analyticsCollector: AnalyticsCollector

    val skiePerformanceAnalyticsProducer: SkiePerformanceAnalytics.Producer

    val kirReporter: KirReporter

    val skiePhaseScheduler: SkiePhaseScheduler

    val compilerShim: CompilerShim

    val skieBuildDirectory: SkieBuildDirectory
        get() = skieDirectories.buildDirectory

    val SkieConfigurationFlag.isEnabled: Boolean
        get() = globalConfiguration.isFlagEnabled(this)

    val SkieConfigurationFlag.isDisabled: Boolean
        get() = this.isEnabled.not()

    fun SkieConfigurationFlag.enable() {
        globalConfiguration.enableFlag(this)
    }

    fun SkieConfigurationFlag.disable() {
        globalConfiguration.disableFlag(this)
    }
}
