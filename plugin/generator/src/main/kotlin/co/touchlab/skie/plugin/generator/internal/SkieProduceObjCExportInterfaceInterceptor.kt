@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.generator.internal

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
        context.config.skieDeclarationBuilder.declareSymbols(context.symbolTable!!)

        return next(context, input)
    }
}
