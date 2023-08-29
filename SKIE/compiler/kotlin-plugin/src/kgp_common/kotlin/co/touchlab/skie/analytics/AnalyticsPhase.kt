package co.touchlab.skie.analytics

import co.touchlab.skie.analytics.compiler.common.CommonCompilerConfigurationAnalytics
import co.touchlab.skie.analytics.compiler.specific.SpecificCompilerConfigurationAnalytics
import co.touchlab.skie.analytics.configuration.SkieConfigurationAnalytics
import co.touchlab.skie.analytics.environment.CompilerEnvironmentAnalytics
import co.touchlab.skie.analytics.modules.ModulesAnalytics
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.SkieCompilationPhase
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class AnalyticsPhase(
    private val config: KonanConfig,
    private val skieContext: SkieContext,
    private val descriptorProvider: DescriptorProvider,
) : SkieCompilationPhase {

    override val isActive: Boolean = true

    override fun runObjcPhase() {
        skieContext.analyticsCollector.collectAsync(
            CommonCompilerConfigurationAnalytics.Producer(config),
            SpecificCompilerConfigurationAnalytics.Producer(config),
            SkieConfigurationAnalytics.Producer(skieContext.skieConfiguration),
            CompilerEnvironmentAnalytics.Producer(config),
        )
    }

    override fun runIrPhase(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext, allModules: Map<String, IrModuleFragment>) {
        skieContext.analyticsCollector.collectSynchronously(
            ModulesAnalytics.Producer(config, allModules, descriptorProvider),
        )
    }
}
