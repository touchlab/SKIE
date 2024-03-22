@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.initPhaseContext
import co.touchlab.skie.compilerinject.compilerplugin.mainSkieContext
import co.touchlab.skie.compilerinject.interceptor.PhaseInterceptor
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.driver.phases.FrontendPhaseOutput
import org.jetbrains.kotlin.backend.konan.driver.phases.ProduceObjCExportInterfacePhase
import org.jetbrains.kotlin.backend.konan.getExportedDependencies
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.backend.konan.objcexport.produceObjCExportInterface

internal class ProduceObjCExportInterfacePhaseInterceptor :
    PhaseInterceptor<PhaseContext, FrontendPhaseOutput.Full, ObjCExportedInterface> {

    override fun getInterceptedPhase(): Any = ProduceObjCExportInterfacePhase

    override fun intercept(
        context: PhaseContext,
        input: FrontendPhaseOutput.Full,
        next: (PhaseContext, FrontendPhaseOutput.Full) -> ObjCExportedInterface,
    ): ObjCExportedInterface {
        fun produceObjCExportInterface() = produceObjCExportInterface(context, input.moduleDescriptor, input.frontendServices)

        val mainSkieContext = EntrypointUtils.createMainSkieContext(
            initPhaseContext = context.config.configuration.initPhaseContext,
            konanConfig = context.config,
            mainModuleDescriptor = input.moduleDescriptor,
            exportedDependencies = lazy { input.moduleDescriptor.getExportedDependencies(context.config) },
            produceObjCExportInterface = ::produceObjCExportInterface,
        )

        EntrypointUtils.runClassExportPhases(mainSkieContext)

        EntrypointUtils.runDescriptorModificationPhases(mainSkieContext)

        return mainSkieContext.objCExportedInterface
    }
}
