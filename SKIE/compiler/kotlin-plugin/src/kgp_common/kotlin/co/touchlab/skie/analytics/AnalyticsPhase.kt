package co.touchlab.skie.analytics

import co.touchlab.skie.analytics.compiler.common.AnonymousCommonCompilerConfigurationAnalytics
import co.touchlab.skie.analytics.compiler.common.IdentifyingCommonCompilerConfigurationAnalytics
import co.touchlab.skie.analytics.compiler.specific.AnonymousSpecificCompilerConfigurationAnalytics
import co.touchlab.skie.analytics.configuration.AnonymousSkieConfigurationAnalytics
import co.touchlab.skie.analytics.configuration.IdentifyingSkieConfigurationAnalytics
import co.touchlab.skie.analytics.environment.AnonymousCompilerEnvironmentAnalytics
import co.touchlab.skie.analytics.modules.AnonymousDeclarationsAnalytics
import co.touchlab.skie.analytics.modules.AnonymousLibrariesAnalytics
import co.touchlab.skie.analytics.modules.IdentifyingLocalModulesAnalytics
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
            AnonymousCommonCompilerConfigurationAnalytics.Producer(config),
            IdentifyingCommonCompilerConfigurationAnalytics.Producer(config),
            AnonymousSpecificCompilerConfigurationAnalytics.Producer(config),
            AnonymousSkieConfigurationAnalytics.Producer(skieContext.skieConfiguration),
            IdentifyingSkieConfigurationAnalytics.Producer(skieContext.skieConfiguration),
            AnonymousCompilerEnvironmentAnalytics.Producer(config),
            IdentifyingLocalModulesAnalytics.Producer(config),
            AnonymousLibrariesAnalytics.Producer(config),
        )
    }

    override fun runIrPhase(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext, allModules: Map<String, IrModuleFragment>) {
        skieContext.analyticsCollector.collectSynchronously(
            AnonymousDeclarationsAnalytics.Producer(config, allModules, descriptorProvider),
        )
    }
}
