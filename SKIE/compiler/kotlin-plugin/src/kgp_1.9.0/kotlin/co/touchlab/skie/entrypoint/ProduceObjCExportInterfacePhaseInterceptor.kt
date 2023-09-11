@file:Suppress("invisible_reference", "invisible_member")
package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.interceptor.PhaseInterceptor
import co.touchlab.skie.compilerinject.reflection.DescriptorProviderKey
import co.touchlab.skie.compilerinject.reflection.MutableDescriptorProviderKey
import co.touchlab.skie.compilerinject.reflection.skieContext
import co.touchlab.skie.phases.SkieCompilationScheduler
import co.touchlab.skie.compilerinject.plugin.SkieCompilerConfigurationKey
import co.touchlab.skie.kir.ExposedModulesProvider
import co.touchlab.skie.kir.NativeMutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
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
        context.config.librariesWithDependencies().forEach { library ->
            if (library.shortName == null) {
                library.manifestProperties.setProperty(
                    KLIB_PROPERTY_SHORT_NAME,
                    library.uniqueName.substringAfterLast(':'),
                )
            }
        }

        val exportedInterface = next(context, input)

        val exposedModulesProvider = ExposedModulesProvider {
            setOf(input.moduleDescriptor) + input.moduleDescriptor.getExportedDependencies(context.config)
        }
        val descriptorProvider = NativeMutableDescriptorProvider(exposedModulesProvider, context.config, exportedInterface)
        context.config.configuration.put(MutableDescriptorProviderKey, descriptorProvider)

        val declarationBuilder = DeclarationBuilderImpl(input.moduleDescriptor, descriptorProvider)
        SkieCompilerConfigurationKey.DeclarationBuilder.put(declarationBuilder, context.config.configuration)

        val skieScheduler = SkieCompilationScheduler(
            config = context.config,
            skieContext = context.config.configuration.skieContext,
            descriptorProvider = descriptorProvider,
            declarationBuilder = declarationBuilder,
        )

        skieScheduler.runClassExportingPhases()

        val updatedExportedInterface = produceObjCExportInterface(context, input.moduleDescriptor, input.frontendServices)

        descriptorProvider.reload(updatedExportedInterface)

        skieScheduler.runObjcPhases()

        SkieCompilerConfigurationKey.SkieScheduler.put(skieScheduler, context.config.configuration)

        val newExportedInterface = produceObjCExportInterface(context, input.moduleDescriptor, input.frontendServices)
        val finalizedDescriptorProvider = descriptorProvider.preventFurtherMutations(newExportedInterface)
        context.config.configuration.put(DescriptorProviderKey, finalizedDescriptorProvider)

        return newExportedInterface
    }
}
