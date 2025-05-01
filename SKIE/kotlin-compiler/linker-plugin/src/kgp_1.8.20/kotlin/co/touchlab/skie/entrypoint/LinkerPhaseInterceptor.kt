@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.mainSkieContext
import co.touchlab.skie.compilerinject.interceptor.SameTypePhaseInterceptor
import kotlin.io.path.absolutePathString
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.linkerPhase

internal class LinkerPhaseInterceptor : SameTypePhaseInterceptor<Context, Unit> {

    override fun getInterceptedPhase(): Any = linkerPhase

    override fun intercept(context: Context, input: Unit, next: (Context, Unit) -> Unit) {
        val mainSkieContext = context.config.configuration.mainSkieContext

        EntrypointUtils.runLinkPhases(mainSkieContext) { additionalObjectFiles ->
            context.generationState.compilerOutput += additionalObjectFiles.map { it.absolutePathString() }

            next(context, Unit)
        }
    }
}
