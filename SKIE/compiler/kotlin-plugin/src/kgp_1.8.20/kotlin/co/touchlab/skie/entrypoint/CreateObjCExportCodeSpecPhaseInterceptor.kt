@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.interceptor.PhaseInterceptor
import co.touchlab.skie.compilerinject.plugin.mainSkieContext
import co.touchlab.skie.phases.SkiePhaseScheduler
import co.touchlab.skie.phases.context.SymbolTablePhaseContext
import org.jetbrains.kotlin.backend.konan.driver.phases.CreateObjCExportCodeSpecPhase
import org.jetbrains.kotlin.backend.konan.driver.phases.PsiToIrContext
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportCodeSpec
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface

internal class CreateObjCExportCodeSpecPhaseInterceptor : PhaseInterceptor<PsiToIrContext, ObjCExportedInterface, ObjCExportCodeSpec> {

    override fun getInterceptedPhase(): Any = CreateObjCExportCodeSpecPhase

    override fun intercept(
        context: PsiToIrContext,
        input: ObjCExportedInterface,
        next: (PsiToIrContext, ObjCExportedInterface) -> ObjCExportCodeSpec,
    ): ObjCExportCodeSpec {
        runSymbolTablePhases(context)

        return next(context, input)
    }

    private fun runSymbolTablePhases(context: PsiToIrContext) {
        val symbolTableContext = SymbolTablePhaseContext(
            mainSkieContext = context.config.configuration.mainSkieContext,
            symbolTable = context.symbolTable!!,
        )

        SkiePhaseScheduler.runSymbolTablePhases(symbolTableContext)
    }
}
