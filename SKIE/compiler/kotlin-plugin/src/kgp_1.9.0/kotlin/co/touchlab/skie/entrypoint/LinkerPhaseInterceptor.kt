@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.mainSkieContext
import co.touchlab.skie.compilerinject.interceptor.PhaseInterceptor
import co.touchlab.skie.util.objectFilePaths
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.driver.phases.LinkerPhase
import org.jetbrains.kotlin.backend.konan.driver.phases.LinkerPhaseInput

internal class LinkerPhaseInterceptor : PhaseInterceptor<PhaseContext, LinkerPhaseInput, Unit> {

    override fun getInterceptedPhase(): Any = LinkerPhase

    override fun intercept(context: PhaseContext, input: LinkerPhaseInput, next: (PhaseContext, LinkerPhaseInput) -> Unit) {
        val inputWithSwiftObjectFiles = input.copy(
            objectFiles = input.objectFiles + context.config.configuration.mainSkieContext.skieDirectories.objectFilePaths,
        )

        next(context, inputWithSwiftObjectFiles)
    }
}
