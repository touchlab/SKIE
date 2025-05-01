@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.mainSkieContext
import co.touchlab.skie.compilerinject.interceptor.SameTypePhaseInterceptor
import co.touchlab.skie.compilerinject.reflection.reflectedBy
import co.touchlab.skie.compilerinject.reflection.reflectors.ObjCExportReflector
import co.touchlab.skie.kir.descriptor.ObjCExportedInterfaceProvider
import org.jetbrains.kotlin.backend.konan.Context as KonanContext
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.backend.konan.objectFilesPhase

internal class ObjectFilesPhaseInterceptor : SameTypePhaseInterceptor<KonanContext, Unit> {

    // Ideally would be org.jetbrains.kotlin.backend.konan.llvm.produceOutputPhase instead, but we don't support interception this kind of phase
    override fun getInterceptedPhase(): Any = objectFilesPhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        val objCExportedInterface = context.objCExport.reflectedBy<ObjCExportReflector>().exportedInterface as ObjCExportedInterface

        val mainSkieContext = context.config.configuration.mainSkieContext

        EntrypointUtils.runKirPhases(
            mainSkieContext = mainSkieContext,
            objCExportedInterfaceProvider = ObjCExportedInterfaceProvider(objCExportedInterface),
        )

        EntrypointUtils.runSirPhases(
            mainSkieContext = mainSkieContext,
        )

        return next(context, input)
    }
}
