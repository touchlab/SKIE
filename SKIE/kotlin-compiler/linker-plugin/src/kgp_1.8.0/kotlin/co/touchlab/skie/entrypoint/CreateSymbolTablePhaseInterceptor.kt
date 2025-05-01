@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.initPhaseContext
import co.touchlab.skie.compilerinject.interceptor.SameTypePhaseInterceptor
import org.jetbrains.kotlin.backend.konan.Context as KonanContext
import org.jetbrains.kotlin.backend.konan.createSymbolTablePhase
import org.jetbrains.kotlin.backend.konan.getExportedDependencies

internal class CreateSymbolTablePhaseInterceptor : SameTypePhaseInterceptor<KonanContext, Unit> {

    override fun getInterceptedPhase(): Any = createSymbolTablePhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        next(context, input)

        val mainSkieContext = EntrypointUtils.createMainSkieContext(
            initPhaseContext = context.config.configuration.initPhaseContext,
            konanConfig = context.config,
            frontendServices = context.frontendServices,
            mainModuleDescriptor = context.moduleDescriptor,
            exportedDependencies = lazy { context.getExportedDependencies() },
        )

        EntrypointUtils.runClassExportPhases(mainSkieContext)
    }
}
