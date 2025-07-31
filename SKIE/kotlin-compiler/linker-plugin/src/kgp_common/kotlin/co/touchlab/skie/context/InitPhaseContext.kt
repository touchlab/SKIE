@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.context

import co.touchlab.skie.analytics.performance.SkiePerformanceAnalytics
import co.touchlab.skie.compilerinject.compilerplugin.SkieConfigurationKeys
import co.touchlab.skie.configuration.GlobalConfiguration
import co.touchlab.skie.configuration.provider.CompilerSkieConfigurationData
import co.touchlab.skie.configuration.provider.ConfigurationProvider
import co.touchlab.skie.phases.InitPhase
import co.touchlab.skie.phases.LinkerPhaseScheduler
import co.touchlab.skie.phases.SkiePhaseScheduler
import co.touchlab.skie.plugin.analytics.AnalyticsCollector
import co.touchlab.skie.sir.compilation.ObjectFileProvider
import co.touchlab.skie.spi.SkiePluginRegistrar
import co.touchlab.skie.util.ActualCompilerShim
import co.touchlab.skie.util.CompilerShim
import co.touchlab.skie.util.DescriptorReporter
import co.touchlab.skie.util.KirReporter
import co.touchlab.skie.util.directory.FrameworkLayout
import co.touchlab.skie.util.directory.SkieDirectories
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.config.CompilerConfiguration

class InitPhaseContext(
    val compilerConfiguration: CompilerConfiguration,
    pluginRegistrars: List<SkiePluginRegistrar>,
) : InitPhase.Context {

    override val context: InitPhase.Context
        get() = this

    override val skiePhaseScheduler: SkiePhaseScheduler = LinkerPhaseScheduler()

    override val skieDirectories: SkieDirectories = compilerConfiguration.getNotNull(SkieConfigurationKeys.SkieDirectories)

    override val skieConfigurationData: CompilerSkieConfigurationData = run {
        val serializedUserConfiguration = skieDirectories.buildDirectory.skieConfiguration.readText()

        CompilerSkieConfigurationData.deserialize(serializedUserConfiguration)
    }

    val configurationProvider = ConfigurationProvider(
        configurationData = skieConfigurationData,
        pluginConfigurationKeys = pluginRegistrars.flatMap { it.customConfigurationKeys }.toSet(),
    )

    override val globalConfiguration: GlobalConfiguration = configurationProvider.globalConfiguration

    override val analyticsCollector: AnalyticsCollector = AnalyticsCollector(
        skieBuildDirectory = skieDirectories.buildDirectory,
        skieConfiguration = skieConfigurationData,
    )

    override val skiePerformanceAnalyticsProducer: SkiePerformanceAnalytics.Producer = SkiePerformanceAnalytics.Producer(globalConfiguration)

    override val kirReporter: KirReporter = KirReporter()

    val descriptorReporter: DescriptorReporter = DescriptorReporter()

    override val compilerShim: CompilerShim = ActualCompilerShim()

    override val objectFileProvider: ObjectFileProvider = ObjectFileProvider(skieBuildDirectory)
}
