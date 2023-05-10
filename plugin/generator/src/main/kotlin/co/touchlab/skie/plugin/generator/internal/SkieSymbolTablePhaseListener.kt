@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal

import co.touchlab.skie.plugin.api.DescriptorProviderKey
import co.touchlab.skie.plugin.api.MutableDescriptorProviderKey
import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.generator.internal.util.NamespaceProvider
import co.touchlab.skie.plugin.generator.internal.util.NativeMutableDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.plugin.intercept.PhaseInterceptor
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.driver.phases.FrontendPhaseOutput
import org.jetbrains.kotlin.backend.konan.driver.phases.PsiToIrContext
import org.jetbrains.kotlin.backend.konan.driver.phases.PsiToIrInput
import org.jetbrains.kotlin.backend.konan.driver.phases.PsiToIrOutput
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.backend.konan.objcexport.produceObjCExportInterface
import org.jetbrains.kotlin.library.KLIB_PROPERTY_SHORT_NAME
import org.jetbrains.kotlin.library.shortName
import org.jetbrains.kotlin.library.uniqueName

internal class SkieSymbolTablePhaseListener : PhaseInterceptor<PhaseContext, FrontendPhaseOutput.Full, ObjCExportedInterface> {

    override val phase = PhaseInterceptor.Phase.ProduceObjCExportInterface

    override fun intercept(
        context: PhaseContext,
        input: FrontendPhaseOutput.Full,
        next: (PhaseContext, FrontendPhaseOutput.Full) -> ObjCExportedInterface
    ): ObjCExportedInterface {
        context.config.librariesWithDependencies(input.moduleDescriptor).forEach { library ->
            if (library.shortName == null) {
                library.manifestProperties.setProperty(
                    KLIB_PROPERTY_SHORT_NAME,
                    library.uniqueName.substringAfterLast(':'),
                )
            }
        }

        val exportedInterface = next(context, input)

        val descriptorProvider = NativeMutableDescriptorProvider(input.moduleDescriptor, context.config, exportedInterface)
        context.config.configuration.put(MutableDescriptorProviderKey, descriptorProvider)

        val declarationBuilder = DeclarationBuilderImpl(input.moduleDescriptor, descriptorProvider)
        SkieCompilerConfigurationKey.DeclarationBuilder.put(declarationBuilder, context.config.configuration)

        val skieScheduler = SkieCompilationScheduler(
            config = context.config,
            skieContext = context.config.skieContext,
            descriptorProvider = descriptorProvider,
            declarationBuilder = declarationBuilder,
            namespaceProvider = NamespaceProvider(context.config.skieContext.module),
            reporter = Reporter(context.config.configuration),
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

internal class SkiePsiToIrPhaseInterceptor: PhaseInterceptor<PsiToIrContext, PsiToIrInput, PsiToIrOutput> {
    override val phase = PhaseInterceptor.Phase.PsiToIr

    override fun intercept(context: PsiToIrContext, input: PsiToIrInput, next: (PsiToIrContext, PsiToIrInput) -> PsiToIrOutput): PsiToIrOutput {
        return next(context, input)
    }
}
