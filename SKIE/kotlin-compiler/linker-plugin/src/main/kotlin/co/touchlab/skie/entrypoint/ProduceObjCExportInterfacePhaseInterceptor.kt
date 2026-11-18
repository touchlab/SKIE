@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.initPhaseContext
import co.touchlab.skie.compilerinject.interceptor.PhaseInterceptor
import co.touchlab.skie.compat.PhaseContext
import co.touchlab.skie.compat.SkieFrontendPhaseOutput
import co.touchlab.skie.kir.descriptor.ObjCExportedInterfaceProvider
import org.jetbrains.kotlin.backend.konan.driver.phases.ProduceObjCExportInterfacePhase
import org.jetbrains.kotlin.backend.konan.getExportedDependencies
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface

internal class ProduceObjCExportInterfacePhaseInterceptor :
    PhaseInterceptor<PhaseContext, SkieFrontendPhaseOutput, ObjCExportedInterface> {

    override fun getInterceptedPhase(): Any = ProduceObjCExportInterfacePhase

    override fun intercept(
        context: PhaseContext,
        input: SkieFrontendPhaseOutput,
        next: (PhaseContext, SkieFrontendPhaseOutput) -> ObjCExportedInterface,
    ): ObjCExportedInterface {
        val konanConfig = context.konanConfig
        val mainSkieContext = EntrypointUtils.createMainSkieContext(
            initPhaseContext = context.config.configuration.initPhaseContext,
            konanConfig = konanConfig,
            frontendPhaseOutput = input,
            mainModuleDescriptor = input.moduleDescriptor,
            exportedDependencies = lazy { input.moduleDescriptor.getExportedDependencies(konanConfig) },
        )

        EntrypointUtils.runClassExportPhases(mainSkieContext)

        EntrypointUtils.runFrontendIrPhases(mainSkieContext)

        val result = next(context, input)

        mainSkieContext.objCExportedInterfaceProvider = ObjCExportedInterfaceProvider(result)

        return result
    }
}
