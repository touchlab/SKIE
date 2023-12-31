@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.mainSkieContext
import co.touchlab.skie.compilerinject.interceptor.SameTypePhaseInterceptor
import co.touchlab.skie.compilerinject.reflection.reflectors.ObjCExportReflector
import org.jetbrains.kotlin.backend.konan.createSymbolTablePhase
import org.jetbrains.kotlin.backend.konan.getExportedDependencies
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

internal class CreateSymbolTablePhaseInterceptor : SameTypePhaseInterceptor<KonanContext, Unit> {

    override fun getInterceptedPhase(): Any = createSymbolTablePhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        next(context, input)

        fun produceObjCExportInterface() = ObjCExportReflector.new(context).exportedInterface as ObjCExportedInterface

        val mainSkieContext = context.config.configuration.mainSkieContext

        mainSkieContext.initialize(
            konanConfig = context.config,
            mainModuleDescriptor = context.moduleDescriptor,
            exportedDependencies = context.getExportedDependencies(),
            produceObjCExportInterface = ::produceObjCExportInterface,
        )

        EntrypointUtils.runClassExportPhases(mainSkieContext, ::produceObjCExportInterface)
    }
}
