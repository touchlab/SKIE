@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.interceptors

import co.touchlab.skie.plugin.generator.internal.skieDeclarationBuilder
import co.touchlab.skie.plugin.intercept.SameTypePhaseInterceptor
import co.touchlab.skie.plugin.reflection.reflectedBy
import co.touchlab.skie.plugin.reflection.reflectors.ContextReflector
import org.jetbrains.kotlin.backend.konan.psiToIrPhase
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

internal class PsiToIrPhaseInterceptor : SameTypePhaseInterceptor<KonanContext, Unit> {

    override fun getInterceptedPhase(): Any = psiToIrPhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        context.config.skieDeclarationBuilder.declareSymbols(context.reflectedBy<ContextReflector>().symbolTable)

        next(context, input)
    }
}
