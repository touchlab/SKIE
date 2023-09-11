@file:Suppress("invisible_reference", "invisible_member")
package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.interceptor.SameTypePhaseInterceptor
import co.touchlab.skie.compilerinject.reflection.DescriptorProviderKey
import co.touchlab.skie.compilerinject.reflection.MutableDescriptorProviderKey
import co.touchlab.skie.compilerinject.plugin.skieScheduler
import co.touchlab.skie.kir.NativeMutableDescriptorProvider
import co.touchlab.skie.compilerinject.reflection.reflectors.ObjCExportReflector
import org.jetbrains.kotlin.backend.konan.Context as KonanContext
import org.jetbrains.kotlin.backend.konan.objCExportPhase
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface

internal class ObjCExportPhaseInterceptor: SameTypePhaseInterceptor<KonanContext, Unit> {
    override fun getInterceptedPhase(): Any = objCExportPhase

    override fun intercept(context: KonanContext, input: Unit, next: (KonanContext, Unit) -> Unit) {
        context.skieScheduler.runObjcPhases()

        val descriptorProvider = context.configuration.getNotNull(MutableDescriptorProviderKey) as NativeMutableDescriptorProvider
        val finalizedDescriptorProvider = descriptorProvider.preventFurtherMutations(
            ObjCExportReflector.new(context).exportedInterface as ObjCExportedInterface,
        )
        context.configuration.put(DescriptorProviderKey, finalizedDescriptorProvider)

        next(context, input)
    }
}
