@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.mainSkieContext
import co.touchlab.skie.compilerinject.interceptor.SameTypePhaseInterceptor
import co.touchlab.skie.compilerinject.reflection.reflectedBy
import co.touchlab.skie.compilerinject.reflection.reflectors.ObjCExportReflector
import co.touchlab.skie.kir.descriptor.ObjCExportedInterfaceProvider
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface

import org.jetbrains.kotlin.backend.konan.objectFilesPhase
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

internal class ObjectFilesPhaseInterceptor : SameTypePhaseInterceptor<KonanContext, Unit> {

    override fun getInterceptedPhase(): Any = objectFilesPhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        next(context, input)

        val objCExportedInterface = context.objCExport.reflectedBy<ObjCExportReflector>().exportedInterface as ObjCExportedInterface

        EntrypointUtils.runSirPhases(
            mainSkieContext = context.config.configuration.mainSkieContext,
            objCExportedInterfaceProvider = ObjCExportedInterfaceProvider(objCExportedInterface),
        )
    }
}
