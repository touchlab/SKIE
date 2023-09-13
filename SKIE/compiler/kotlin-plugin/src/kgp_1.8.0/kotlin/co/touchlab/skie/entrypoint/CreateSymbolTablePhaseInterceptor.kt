@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.interceptor.SameTypePhaseInterceptor
import co.touchlab.skie.compilerinject.plugin.mainSkieContext
import co.touchlab.skie.compilerinject.reflection.reflectors.ObjCExportReflector
import co.touchlab.skie.kir.ExposedModulesProvider
import co.touchlab.skie.kir.NativeMutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.SkiePhaseScheduler
import co.touchlab.skie.phases.context.ClassExportPhaseContext
import co.touchlab.skie.phases.context.MainSkieContext
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.createSymbolTablePhase
import org.jetbrains.kotlin.backend.konan.getExportedDependencies
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

internal class CreateSymbolTablePhaseInterceptor : SameTypePhaseInterceptor<KonanContext, Unit> {

    override fun getInterceptedPhase(): Any = createSymbolTablePhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        next(context, input)

        val mainSkieContext = context.config.configuration.mainSkieContext

        mainSkieContext.initialize(context)

        runClassExportPhases(mainSkieContext, context)
    }

    private fun MainSkieContext.initialize(context: KonanContext) {
        val exposedModulesProvider = ExposedModulesProvider {
            setOf(context.moduleDescriptor) + context.getExportedDependencies()
        }

        this.descriptorProvider = NativeMutableDescriptorProvider(
            exposedModulesProvider,
            context.config,
            ObjCExportReflector.new(context).exportedInterface as ObjCExportedInterface,
        )

        this.declarationBuilder = DeclarationBuilderImpl(context.moduleDescriptor, this.descriptorProvider)
    }

    private fun runClassExportPhases(
        mainSkieContext: MainSkieContext,
        context: Context,
    ) {
        val classExportPhaseContext = ClassExportPhaseContext(mainSkieContext)
        SkiePhaseScheduler.runClassExportPhases(classExportPhaseContext)

        val updatedExportedInterface = ObjCExportReflector.new(context).exportedInterface as ObjCExportedInterface
        mainSkieContext.reloadDescriptorProvider(updatedExportedInterface)
    }
}
