package co.touchlab.skie.phases.analytics

import co.touchlab.skie.phases.analytics.compiler.common.CommonCompilerConfigurationAnalytics
import co.touchlab.skie.phases.analytics.compiler.specific.SpecificCompilerConfigurationAnalytics
import co.touchlab.skie.phases.analytics.configuration.SkieConfigurationAnalytics
import co.touchlab.skie.phases.analytics.environment.CompilerEnvironmentAnalytics
import co.touchlab.skie.phases.analytics.modules.ModulesAnalytics
import co.touchlab.skie.phases.SkieContext
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.phases.SkieCompilationPhase
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
