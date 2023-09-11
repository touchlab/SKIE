@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.plugin.skieDeclarationBuilder
import co.touchlab.skie.compilerinject.interceptor.SameTypePhaseInterceptor
import co.touchlab.skie.compilerinject.reflection.reflectedBy
import co.touchlab.skie.compilerinject.reflection.reflectors.ContextReflector
import org.jetbrains.kotlin.backend.konan.psiToIrPhase
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

internal class PsiToIrPhaseInterceptor : SameTypePhaseInterceptor<KonanContext, Unit> {

    override fun getInterceptedPhase(): Any = psiToIrPhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        context.config.skieDeclarationBuilder.declareSymbols(context.reflectedBy<ContextReflector>().symbolTable)

        next(context, input)
    }
}
