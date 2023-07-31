@file:Suppress("invisible_reference", "invisible_member")
package co.touchlab.skie.plugin.interceptors

import co.touchlab.skie.osversion.MinOSVersionConfigurator
import co.touchlab.skie.plugin.intercept.PhaseInterceptor
import org.jetbrains.kotlin.backend.konan.driver.phases.PsiToIrContext
import org.jetbrains.kotlin.backend.konan.driver.phases.PsiToIrInput
import org.jetbrains.kotlin.backend.konan.driver.phases.PsiToIrOutput
import org.jetbrains.kotlin.backend.konan.driver.phases.PsiToIrPhase

internal class PsiToIrPhaseInterceptor: PhaseInterceptor<PsiToIrContext, PsiToIrInput, PsiToIrOutput> {
    override fun getInterceptedPhase(): Any = PsiToIrPhase

    override fun intercept(context: PsiToIrContext, input: PsiToIrInput, next: (PsiToIrContext, PsiToIrInput) -> PsiToIrOutput): PsiToIrOutput {
        MinOSVersionConfigurator.configure(context.config.configuration, context.config)

        return next(context, input)
    }
}
