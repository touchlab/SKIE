@file:Suppress("invisible_reference", "invisible_member")
package co.touchlab.skie.plugin.interceptors

import co.touchlab.skie.osversion.MinOSVersionConfigurator
import co.touchlab.skie.plugin.generator.internal.skieDeclarationBuilder
import co.touchlab.skie.plugin.intercept.SameTypePhaseInterceptor
import co.touchlab.skie.plugin.reflection.reflectedBy
import co.touchlab.skie.plugin.reflection.reflectors.ContextReflector
import org.jetbrains.kotlin.backend.konan.Context as KonanContext
import org.jetbrains.kotlin.backend.konan.psiToIrPhase

internal class PsiToIrPhaseInterceptor: SameTypePhaseInterceptor<KonanContext, Unit> {
    override fun getInterceptedPhase(): Any = psiToIrPhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        MinOSVersionConfigurator.configure(context.configuration, context.config)
        context.config.skieDeclarationBuilder.declareSymbols(context.reflectedBy<ContextReflector>().symbolTable)

        next(context, input)
    }
}
