@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal

import co.touchlab.skie.plugin.api.MutableDescriptorProviderKey
import co.touchlab.skie.plugin.generator.internal.util.NativeMutableDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.plugin.intercept.PhaseInterceptor
import co.touchlab.skie.plugin.intercept.PhaseListener
import co.touchlab.skie.plugin.reflection.reflectedBy
import co.touchlab.skie.plugin.reflection.reflectors.ContextReflector
import co.touchlab.skie.plugin.reflection.reflectors.ObjCExportReflector
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
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

        val exportedInferface = next(context, input)

        val descriptorProvider = NativeMutableDescriptorProvider(exportedInferface) {
            produceObjCExportInterface(context, input.moduleDescriptor, input.frontendServices)
        }

        context.config.configuration.put(MutableDescriptorProviderKey, descriptorProvider)
        SkieCompilerConfigurationKey.MutableDescriptorProvider.put(descriptorProvider, context.config.configuration)

        return exportedInferface
    }
}

internal class SkiePsiToIrPhaseInterceptor: PhaseInterceptor<PsiToIrContext, PsiToIrInput, PsiToIrOutput> {
    override val phase = PhaseInterceptor.Phase.PsiToIr

    override fun intercept(context: PsiToIrContext, input: PsiToIrInput, next: (PsiToIrContext, PsiToIrInput) -> PsiToIrOutput): PsiToIrOutput {
        return next(context, input)
    }
}
