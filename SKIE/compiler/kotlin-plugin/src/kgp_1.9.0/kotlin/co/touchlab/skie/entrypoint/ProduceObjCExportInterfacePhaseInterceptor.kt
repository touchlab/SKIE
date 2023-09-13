@file:Suppress("invisible_reference", "invisible_member")
package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.interceptor.PhaseInterceptor
import co.touchlab.skie.compilerinject.reflection.DescriptorProviderKey
import co.touchlab.skie.compilerinject.reflection.MutableDescriptorProviderKey
import co.touchlab.skie.compilerinject.reflection.skieContext
import co.touchlab.skie.phases.SkieCompilationScheduler
import co.touchlab.skie.compilerinject.plugin.SkieCompilerConfigurationKey
import co.touchlab.skie.compilerinject.plugin.mainSkieContext
import co.touchlab.skie.kir.ExposedModulesProvider
import co.touchlab.skie.kir.NativeMutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.SkiePhaseScheduler
import co.touchlab.skie.phases.context.ClassExportPhaseContext
import co.touchlab.skie.phases.context.DescriptorModificationPhaseContext
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.driver.phases.FrontendPhaseOutput
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.backend.konan.driver.phases.ProduceObjCExportInterfacePhase
import org.jetbrains.kotlin.backend.konan.getExportedDependencies
import org.jetbrains.kotlin.library.KLIB_PROPERTY_SHORT_NAME
import org.jetbrains.kotlin.library.shortName
import org.jetbrains.kotlin.library.uniqueName
import org.jetbrains.kotlin.backend.konan.objcexport.produceObjCExportInterface

internal class ProduceObjCExportInterfacePhaseInterceptor: PhaseInterceptor<PhaseContext, FrontendPhaseOutput.Full, ObjCExportedInterface> {
    override fun getInterceptedPhase(): Any = ProduceObjCExportInterfacePhase

    override fun intercept(
        context: PhaseContext,
        input: FrontendPhaseOutput.Full,
        next: (PhaseContext, FrontendPhaseOutput.Full) -> ObjCExportedInterface,
    ): ObjCExportedInterface {
        val exportedInterface = next(context, input)

        val exposedModulesProvider = ExposedModulesProvider {
            setOf(input.moduleDescriptor) + input.moduleDescriptor.getExportedDependencies(context.config)
        }
        val descriptorProvider = NativeMutableDescriptorProvider(exposedModulesProvider, context.config, exportedInterface)
        context.config.configuration.put(MutableDescriptorProviderKey, descriptorProvider)


        // WIP
        val mainSkieContext = context.config.configuration.mainSkieContext

        mainSkieContext.declarationBuilder = DeclarationBuilderImpl(input.moduleDescriptor, descriptorProvider)



        val classExportPhaseContext = ClassExportPhaseContext(
            mainSkieContext = mainSkieContext,
        )
        SkiePhaseScheduler.runClassExportPhases(classExportPhaseContext)

        val updatedExportedInterface = produceObjCExportInterface(context, input.moduleDescriptor, input.frontendServices)
        mainSkieContext.reloadDescriptorProvider(updatedExportedInterface)





        val descriptorModificationPhaseContext = DescriptorModificationPhaseContext(
            mainSkieContext = mainSkieContext,
        )
        SkiePhaseScheduler.runDescriptorModificationPhases(descriptorModificationPhaseContext)

        val newExportedInterface = produceObjCExportInterface(context, input.moduleDescriptor, input.frontendServices)
        mainSkieContext.finalizeDescriptorProvider(newExportedInterface)

        return newExportedInterface
    }
}
