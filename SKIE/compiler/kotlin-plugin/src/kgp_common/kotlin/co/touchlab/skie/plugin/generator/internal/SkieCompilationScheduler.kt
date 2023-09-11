package co.touchlab.skie.plugin.generator.internal

import co.touchlab.skie.analytics.AnalyticsPhase
import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.kotlin.MutableDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.arguments.DefaultArgumentGenerator
import co.touchlab.skie.plugin.generator.internal.coroutines.VerifyMinOSVersionPhase
import co.touchlab.skie.plugin.generator.internal.coroutines.flow.FlowBridgingConfigurator
import co.touchlab.skie.plugin.generator.internal.coroutines.flow.FlowConversionConstructorsGenerator
import co.touchlab.skie.plugin.generator.internal.coroutines.flow.FlowMappingConfigurator
import co.touchlab.skie.plugin.generator.internal.coroutines.suspend.SuspendGenerator
import co.touchlab.skie.plugin.generator.internal.enums.ExhaustiveEnumsGenerator
import co.touchlab.skie.plugin.generator.internal.export.ExtraClassExportPhase
import co.touchlab.skie.plugin.generator.internal.runtime.KotlinRuntimeHidingPhase
import co.touchlab.skie.plugin.generator.internal.runtime.SwiftRuntimeGenerator
import co.touchlab.skie.plugin.generator.internal.sealed.SealedInteropGenerator
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.GenerateIrPhase
import co.touchlab.skie.plugin.generator.internal.validation.IrValidator
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class SkieCompilationScheduler(
    config: KonanConfig,
    private val skieContext: SkieContext,
    descriptorProvider: MutableDescriptorProvider,
    declarationBuilder: DeclarationBuilderImpl,
) {

    private val compilationPhases = listOf(
        AnalyticsPhase(
            config = config,
            skieContext = skieContext,
            descriptorProvider = descriptorProvider,
        ),
        VerifyMinOSVersionPhase(
            configuration = skieContext.skieConfiguration,
            konanConfig = config,
        ),
        ExtraClassExportPhase(
            skieContext = skieContext,
            descriptorProvider = descriptorProvider,
            declarationBuilder = declarationBuilder,
        ),
        GenerateIrPhase(
            declarationBuilder = declarationBuilder,
        ),
        IrValidator(
            skieContext = skieContext,
            descriptorProvider = descriptorProvider,
        ),
        KotlinRuntimeHidingPhase(
            skieContext = skieContext,
            descriptorProvider = descriptorProvider,
        ),
        FlowBridgingConfigurator(
            skieContext = skieContext,
        ),
        FlowConversionConstructorsGenerator(
            skieContext = skieContext,
        ),
        FlowMappingConfigurator(
            skieContext = skieContext,
        ),
        SwiftRuntimeGenerator(
            skieContext = skieContext,
        ),
        SealedInteropGenerator(
            skieContext = skieContext,
        ),
        DefaultArgumentGenerator(
            skieContext = skieContext,
            descriptorProvider = descriptorProvider,
            declarationBuilder = declarationBuilder,
        ),
        ExhaustiveEnumsGenerator(
            skieContext = skieContext,
        ),
        SuspendGenerator(
            skieContext = skieContext,
            descriptorProvider = descriptorProvider,
            declarationBuilder = declarationBuilder,
        ),
    )

    fun runClassExportingPhases() {
        compilationPhases
            .filter { it.isActive }
            .forEach {
                skieContext.skiePerformanceAnalyticsProducer.log("Class Exporting Phase: ${it::class.simpleName}") {
                    it.runClassExportingPhase()
                }
            }
    }

    fun runObjcPhases() {
        compilationPhases
            .filter { it.isActive }
            .forEach {
                skieContext.skiePerformanceAnalyticsProducer.log("ObjC Phase: ${it::class.simpleName}") {
                    it.runObjcPhase()
                }
            }
    }

    fun runIrPhases(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext, allModules: Map<String, IrModuleFragment>) {
        compilationPhases
            .filter { it.isActive }
            .forEach {
                skieContext.skiePerformanceAnalyticsProducer.log("IR Phase: ${it::class.simpleName}") {
                    it.runIrPhase(moduleFragment, pluginContext, allModules)
                }
            }
    }
}
