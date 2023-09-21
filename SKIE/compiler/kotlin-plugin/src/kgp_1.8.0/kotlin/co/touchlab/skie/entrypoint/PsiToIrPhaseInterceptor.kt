@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.interceptor.SameTypePhaseInterceptor
import co.touchlab.skie.compilerinject.plugin.mainSkieContext
import co.touchlab.skie.compilerinject.reflection.reflectedBy
import co.touchlab.skie.compilerinject.reflection.reflectors.ContextReflector
import co.touchlab.skie.phases.SkiePhaseScheduler
import co.touchlab.skie.context.SymbolTablePhaseContext
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.psiToIrPhase
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

internal class PsiToIrPhaseInterceptor : SameTypePhaseInterceptor<KonanContext, Unit> {

    override fun getInterceptedPhase(): Any = psiToIrPhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        EntrypointUtils.runSymbolTablePhases(
            mainSkieContext = context.config.configuration.mainSkieContext,
            symbolTable = context.reflectedBy<ContextReflector>().symbolTable,
        )

        next(context, input)
    }
}
