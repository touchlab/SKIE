@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.mainSkieContext
import co.touchlab.skie.compilerinject.interceptor.SameTypePhaseInterceptor
import co.touchlab.skie.util.objectFilePaths
import org.jetbrains.kotlin.backend.konan.objectFilesPhase
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

internal class ObjectFilesPhaseInterceptor : SameTypePhaseInterceptor<KonanContext, Unit> {

    override fun getInterceptedPhase(): Any = objectFilesPhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        next(context, input)

        val mainSkieContext = context.config.configuration.mainSkieContext

        EntrypointUtils.runSirPhases(mainSkieContext)

        context.compilerOutput += mainSkieContext.skieDirectories.objectFilePaths
    }
}
