package co.touchlab.skie.plugin.generator.internal.analytics

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.skieBuildDirectory
import co.touchlab.skie.plugin.generator.internal.analytics.air.AirAnalyticsProducer
import co.touchlab.skie.plugin.generator.internal.analytics.compiler.CompilerAnalyticsProducer
import co.touchlab.skie.plugin.generator.internal.analytics.configuration.SkieConfigurationAnalyticsProducer
import co.touchlab.skie.plugin.generator.internal.analytics.git.GitRemotesAnalyticsProducer
import co.touchlab.skie.plugin.generator.internal.analytics.hw.HardwareAnalyticsProducer
import co.touchlab.skie.plugin.generator.internal.analytics.system.SysctlAnalyticsProducer
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
        val producers = listOf(
            CompilerAnalyticsProducer(config),
            SkieConfigurationAnalyticsProducer(skieContext.skieConfiguration),
            SysctlAnalyticsProducer,
            HardwareAnalyticsProducer,
            GitRemotesAnalyticsProducer(skieContext.skieBuildDirectory),
        )

        skieContext.analyticsCollector.collect(producers)
    }

    override fun runIrPhase(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext, allModules: Map<String, IrModuleFragment>) {
        val producers = listOf(
            AirAnalyticsProducer(descriptorProvider, allModules.values.toList()),
        )

        // WIP
//         skieContext.analyticsCollector.collect(producers)
    }
}
