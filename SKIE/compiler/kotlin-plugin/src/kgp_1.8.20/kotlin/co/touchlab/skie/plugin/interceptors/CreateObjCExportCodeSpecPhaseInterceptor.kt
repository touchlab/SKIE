@file:Suppress("invisible_reference", "invisible_member")
package co.touchlab.skie.plugin.interceptors

import co.touchlab.skie.plugin.generator.internal.skieDeclarationBuilder
import org.jetbrains.kotlin.backend.konan.driver.phases.PsiToIrContext
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportCodeSpec
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.backend.konan.driver.phases.CreateObjCExportCodeSpecPhase

import co.touchlab.skie.plugin.intercept.PhaseInterceptor



internal class CreateObjCExportCodeSpecPhaseInterceptor: PhaseInterceptor<PsiToIrContext, ObjCExportedInterface, ObjCExportCodeSpec> {
    override fun getInterceptedPhase(): Any = CreateObjCExportCodeSpecPhase

    override fun intercept(
        context: PsiToIrContext,
        input: ObjCExportedInterface,
        next: (PsiToIrContext, ObjCExportedInterface) -> ObjCExportCodeSpec,
    ): ObjCExportCodeSpec {
        context.config.skieDeclarationBuilder.declareSymbols(context.symbolTable!!)

        return next(context, input)
    }
}
