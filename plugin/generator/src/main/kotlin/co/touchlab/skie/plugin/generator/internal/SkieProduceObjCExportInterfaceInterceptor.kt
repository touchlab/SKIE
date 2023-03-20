@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal

import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.generator.internal.util.NamespaceProvider
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.plugin.intercept.PhaseInterceptor
import org.jetbrains.kotlin.backend.konan.driver.phases.PsiToIrContext
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportCodeSpec
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface

internal class SkieProduceObjCExportInterfaceInterceptor : PhaseInterceptor<PsiToIrContext, ObjCExportedInterface, ObjCExportCodeSpec> {

    override val phase = PhaseInterceptor.Phase.CreateObjCExportCodeSpec

    override fun intercept(
        context: PsiToIrContext,
        input: ObjCExportedInterface,
        next: (PsiToIrContext, ObjCExportedInterface) -> ObjCExportCodeSpec,
    ): ObjCExportCodeSpec {
        val declarationBuilder =
            DeclarationBuilderImpl(context.symbolTable!!, context.builtIns.builtInsModule, context.config.skieInternalMutableDescriptorProvider)
        SkieCompilerConfigurationKey.DeclarationBuilder.put(declarationBuilder, context.config.configuration)

        val skieScheduler = SkieCompilationScheduler(
            config = context.config,
            skieContext = context.config.skieContext,
            descriptorProvider = context.config.skieInternalMutableDescriptorProvider,
            declarationBuilder = context.config.skieDeclarationBuilder,
            namespaceProvider = NamespaceProvider(context.config.skieContext.module),
            reporter = Reporter(context.config.configuration),
        )

        SkieCompilerConfigurationKey.SkieScheduler.put(skieScheduler, context.config.configuration)

        skieScheduler.runObjcPhases()

        // TODO: This is terrible, we shouldn't be breaking encapsulation like this!!!
        val newExportedInterface = context.config.skieInternalMutableDescriptorProvider.exportedInterfaceProvider()

        val codespec = next(context, newExportedInterface)
        return codespec
    }
}
