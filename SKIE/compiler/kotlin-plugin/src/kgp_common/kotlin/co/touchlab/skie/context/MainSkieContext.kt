@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.context

import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.kir.descriptor.NativeMutableDescriptorProvider
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.InitPhase
import co.touchlab.skie.phases.SkiePhase
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportedInterface
import org.jetbrains.kotlin.descriptors.ModuleDescriptor

class MainSkieContext internal constructor(
    initPhaseContext: InitPhase.Context,
    override val konanConfig: KonanConfig,
    val mainModuleDescriptor: ModuleDescriptor,
    exportedDependencies: Collection<ModuleDescriptor>,
    produceObjCExportInterface: () -> ObjCExportedInterface,
) : SkiePhase.Context, InitPhase.Context by initPhaseContext {

    override val context: SkiePhase.Context
        get() = this

    private val nativeMutableDescriptorProvider = NativeMutableDescriptorProvider(
        exposedModulesProvider = {
            setOf(mainModuleDescriptor) + exportedDependencies
        },
        config = konanConfig,
        produceObjCExportInterface = produceObjCExportInterface,
    )

    override val descriptorProvider: MutableDescriptorProvider by ::nativeMutableDescriptorProvider

    val declarationBuilder: DeclarationBuilderImpl = DeclarationBuilderImpl(mainModuleDescriptor, nativeMutableDescriptorProvider)

    internal val objCExportedInterface: ObjCExportedInterface
        get() = nativeMutableDescriptorProvider.objCExportedInterface

    val namer: ObjCExportNamer
        get() = nativeMutableDescriptorProvider.objCExportedInterface.namer
}
