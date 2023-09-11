package co.touchlab.skie.phases

import co.touchlab.skie.phases.analytics.AnalyticsPhase
import co.touchlab.skie.kir.MutableDescriptorProvider
import co.touchlab.skie.phases.features.defaultarguments.DefaultArgumentGenerator
import co.touchlab.skie.phases.features.flow.FlowBridgingConfigurator
import co.touchlab.skie.phases.features.flow.FlowConversionConstructorsGenerator
import co.touchlab.skie.phases.features.flow.FlowMappingConfigurator
import co.touchlab.skie.phases.features.suspend.SuspendGenerator
import co.touchlab.skie.phases.features.enums.ExhaustiveEnumsGenerator
import co.touchlab.skie.phases.runtime.KotlinRuntimeHidingPhase
import co.touchlab.skie.phases.runtime.SwiftRuntimeGenerator
import co.touchlab.skie.phases.features.sealed.SealedInteropGenerator
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.kir.irbuilder.impl.GenerateIrPhase
import co.touchlab.skie.phases.validation.IrValidator
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
