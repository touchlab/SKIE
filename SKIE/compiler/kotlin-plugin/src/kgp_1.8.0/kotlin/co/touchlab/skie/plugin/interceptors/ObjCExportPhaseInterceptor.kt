@file:Suppress("invisible_reference", "invisible_member")
package co.touchlab.skie.plugin.interceptors

import co.touchlab.skie.plugin.api.DescriptorProviderKey
import co.touchlab.skie.plugin.api.MutableDescriptorProviderKey
import co.touchlab.skie.plugin.generator.internal.skieDeclarationBuilder
import co.touchlab.skie.plugin.generator.internal.skieScheduler
import co.touchlab.skie.plugin.generator.internal.util.NativeDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.NativeMutableDescriptorProvider
import co.touchlab.skie.plugin.intercept.SameTypePhaseInterceptor
import co.touchlab.skie.plugin.reflection.reflectedBy
import co.touchlab.skie.plugin.reflection.reflectors.ContextReflector
import co.touchlab.skie.plugin.reflection.reflectors.ObjCExportReflector
import org.jetbrains.kotlin.backend.konan.Context
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
