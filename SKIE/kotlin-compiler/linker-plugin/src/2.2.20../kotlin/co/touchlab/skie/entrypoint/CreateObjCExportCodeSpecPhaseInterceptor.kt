@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.mainSkieContext
import co.touchlab.skie.compilerinject.interceptor.PhaseInterceptor
import co.touchlab.skie.kir.descriptor.ObjCExportedInterfaceProvider
import org.jetbrains.kotlin.backend.konan.LinkKlibsContext
import org.jetbrains.kotlin.backend.konan.driver.phases.CreateObjCExportCodeSpecPhase
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportCodeSpec
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface

internal class CreateObjCExportCodeSpecPhaseInterceptor : PhaseInterceptor<LinkKlibsContext, ObjCExportedInterface, ObjCExportCodeSpec> {

    override fun getInterceptedPhase(): Any = CreateObjCExportCodeSpecPhase

    override fun intercept(
        context: LinkKlibsContext,
        input: ObjCExportedInterface,
        next: (LinkKlibsContext, ObjCExportedInterface) -> ObjCExportCodeSpec,
    ): ObjCExportCodeSpec {
        val mainSkieContext = context.config.configuration.mainSkieContext

        EntrypointUtils.runSymbolTablePhases(
            mainSkieContext = mainSkieContext,
            symbolTable = context.symbolTable!!,
        )

        EntrypointUtils.runKirPhases(
            mainSkieContext = mainSkieContext,
            objCExportedInterfaceProvider = ObjCExportedInterfaceProvider(input),
        )

        EntrypointUtils.runSirPhases(mainSkieContext)

        return next(context, input)
    }
}
