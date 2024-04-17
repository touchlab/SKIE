@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.mainSkieContext
import co.touchlab.skie.compilerinject.interceptor.SameTypePhaseInterceptor
import org.jetbrains.kotlin.backend.konan.objCExportPhase
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

internal class ObjCExportPhaseInterceptor : SameTypePhaseInterceptor<KonanContext, Unit> {

    override fun getInterceptedPhase(): Any = objCExportPhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        EntrypointUtils.runDescriptorModificationPhases(context.config.configuration.mainSkieContext)

        next(context, input)
    }
}
