@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.interceptor.PhaseInterceptor
import co.touchlab.skie.compilerinject.compilerplugin.mainSkieContext
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
        val mainSkieContext = context.config.configuration.mainSkieContext

        EntrypointUtils.runSymbolTablePhases(
            mainSkieContext = mainSkieContext,
            symbolTable = context.symbolTable!!,
        )

        EntrypointUtils.runSirPhases(mainSkieContext)

        return next(context, input)
    }
}
