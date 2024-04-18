@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.context

import co.touchlab.skie.analytics.performance.SkiePerformanceAnalytics
import co.touchlab.skie.compilerinject.compilerplugin.SkieConfigurationKeys
import co.touchlab.skie.configuration.RootConfiguration
import co.touchlab.skie.configuration.provider.CompilerSkieConfigurationData
import co.touchlab.skie.configuration.provider.ConfigurationProvider
import co.touchlab.skie.phases.InitPhase
import co.touchlab.skie.phases.SkiePhaseScheduler
import co.touchlab.skie.plugin.analytics.AnalyticsCollector
import co.touchlab.skie.util.ActualCompilerShim
import co.touchlab.skie.util.CompilerShim
import co.touchlab.skie.util.FrameworkLayout
import co.touchlab.skie.util.Reporter
import co.touchlab.skie.util.directory.SkieDirectories
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.config.CompilerConfiguration

class InitPhaseContext(
    val compilerConfiguration: CompilerConfiguration,
) : InitPhase.Context {

    override val context: InitPhase.Context
        get() = this

    val skiePhaseScheduler: SkiePhaseScheduler = SkiePhaseScheduler()

    override val skieDirectories: SkieDirectories = compilerConfiguration.getNotNull(SkieConfigurationKeys.SkieDirectories)

    override val skieConfigurationData: CompilerSkieConfigurationData = run {
        val serializedUserConfiguration = skieDirectories.buildDirectory.skieConfiguration.readText()

        CompilerSkieConfigurationData.deserialize(serializedUserConfiguration)
    }

    val configurationProvider = ConfigurationProvider(skieConfigurationData)

    override val rootConfiguration: RootConfiguration = configurationProvider.rootConfiguration

    override val analyticsCollector: AnalyticsCollector = AnalyticsCollector(
        skieBuildDirectory = skieDirectories.buildDirectory,
        skieConfiguration = skieConfigurationData,
    )

    override val skiePerformanceAnalyticsProducer: SkiePerformanceAnalytics.Producer = SkiePerformanceAnalytics.Producer(rootConfiguration)

    override val reporter: Reporter = Reporter()

    override val compilerShim: CompilerShim = ActualCompilerShim()

    override val framework: FrameworkLayout = run {
        val frameworkPath = compilerConfiguration.getNotNull(KonanConfigKeys.OUTPUT)

        FrameworkLayout(frameworkPath)
    }
}
