@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.interceptor.SameTypePhaseInterceptor
import co.touchlab.skie.compilerinject.plugin.mainSkieContext
import co.touchlab.skie.compilerinject.reflection.reflectors.ObjCExportReflector
import co.touchlab.skie.phases.SkiePhaseScheduler
import co.touchlab.skie.phases.context.DescriptorModificationPhaseContext
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.objCExportPhase
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

internal class ObjCExportPhaseInterceptor : SameTypePhaseInterceptor<KonanContext, Unit> {

    override fun getInterceptedPhase(): Any = objCExportPhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        runDescriptorModificationPhases(context)

        next(context, input)
    }

    private fun runDescriptorModificationPhases(context: Context) {
        val mainSkieContext = context.config.configuration.mainSkieContext

        val descriptorModificationPhaseContext = DescriptorModificationPhaseContext(mainSkieContext)
        SkiePhaseScheduler.runDescriptorModificationPhases(descriptorModificationPhaseContext)

        val objCExportedInterface = ObjCExportReflector.new(context).exportedInterface as ObjCExportedInterface
        mainSkieContext.finalizeDescriptorProvider(objCExportedInterface)
    }
}
