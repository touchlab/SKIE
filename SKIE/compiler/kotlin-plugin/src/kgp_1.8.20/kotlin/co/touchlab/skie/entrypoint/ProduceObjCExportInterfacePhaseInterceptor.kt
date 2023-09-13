@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.interceptor.PhaseInterceptor
import co.touchlab.skie.compilerinject.plugin.mainSkieContext
import co.touchlab.skie.compilerinject.reflection.reflectors.ObjCExportReflector
import co.touchlab.skie.kir.ExposedModulesProvider
import co.touchlab.skie.kir.NativeMutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.SkiePhaseScheduler
import co.touchlab.skie.phases.context.ClassExportPhaseContext
import co.touchlab.skie.phases.context.DescriptorModificationPhaseContext
import co.touchlab.skie.phases.context.MainSkieContext
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.driver.phases.FrontendPhaseOutput
import org.jetbrains.kotlin.backend.konan.driver.phases.ProduceObjCExportInterfacePhase
import org.jetbrains.kotlin.backend.konan.getExportedDependencies
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.backend.konan.objcexport.produceObjCExportInterface

internal class ProduceObjCExportInterfacePhaseInterceptor :
    PhaseInterceptor<PhaseContext, FrontendPhaseOutput.Full, ObjCExportedInterface> {

    override fun getInterceptedPhase(): Any = ProduceObjCExportInterfacePhase

    override fun intercept(
        context: PhaseContext,
        input: FrontendPhaseOutput.Full,
        next: (PhaseContext, FrontendPhaseOutput.Full) -> ObjCExportedInterface,
    ): ObjCExportedInterface {
        val exportedInterface = next(context, input)

        val mainSkieContext = context.config.configuration.mainSkieContext

        mainSkieContext.initialize(context)

        runClassExportPhases(mainSkieContext, input, context)

        return runDescriptorModificationPhases(mainSkieContext, context, input)
    }

    private fun MainSkieContext.initialize(context: Context, input: FrontendPhaseOutput.Full) {

        val exposedModulesProvider = ExposedModulesProvider {
            setOf(input.moduleDescriptor) + input.moduleDescriptor.getExportedDependencies(context.config)
        }
        val descriptorProvider = NativeMutableDescriptorProvider(exposedModulesProvider, context.config, exportedInterface)
        context.config.configuration.put(MutableDescriptorProviderKey, descriptorProvider)

        // WIP
        mainSkieContext.declarationBuilder = DeclarationBuilderImpl(input.moduleDescriptor, descriptorProvider)


        val exposedModulesProvider = ExposedModulesProvider {
            setOf(context.moduleDescriptor) + context.getExportedDependencies()
        }

        this.descriptorProvider = NativeMutableDescriptorProvider(
            exposedModulesProvider,
            context.config,
            ObjCExportReflector.new(context).exportedInterface as ObjCExportedInterface,
        )

        this.declarationBuilder = DeclarationBuilderImpl(context.moduleDescriptor, this.descriptorProvider)
    }

    private fun runClassExportPhases(
        mainSkieContext: MainSkieContext,
        input: FrontendPhaseOutput.Full,
        context: PhaseContext,
    ) {
        val classExportPhaseContext = ClassExportPhaseContext(mainSkieContext)
        SkiePhaseScheduler.runClassExportPhases(classExportPhaseContext)

        val updatedExportedInterface = produceObjCExportInterface(context, input.moduleDescriptor, input.frontendServices)
        mainSkieContext.reloadDescriptorProvider(updatedExportedInterface)
    }

    private fun runDescriptorModificationPhases(
        mainSkieContext: MainSkieContext,
        context: PhaseContext,
        input: FrontendPhaseOutput.Full,
    ): ObjCExportedInterface {
        val descriptorModificationPhaseContext = DescriptorModificationPhaseContext(mainSkieContext)
        SkiePhaseScheduler.runDescriptorModificationPhases(descriptorModificationPhaseContext)

        val finalExportedInterface = produceObjCExportInterface(context, input.moduleDescriptor, input.frontendServices)
        mainSkieContext.finalizeDescriptorProvider(finalExportedInterface)

        return finalExportedInterface
    }
}
