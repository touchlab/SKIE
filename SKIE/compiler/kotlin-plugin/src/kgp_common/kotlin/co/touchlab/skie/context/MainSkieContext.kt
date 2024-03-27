@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.context

import co.touchlab.skie.kir.descriptor.MutableDescriptorProvider
import co.touchlab.skie.kir.descriptor.NativeDescriptorProvider
import co.touchlab.skie.kir.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.phases.InitPhase
import co.touchlab.skie.phases.SkiePhase
import org.jetbrains.kotlin.backend.konan.FrontendServices
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.descriptors.ModuleDescriptor

class MainSkieContext internal constructor(
    initPhaseContext: InitPhase.Context,
    override val konanConfig: KonanConfig,
    frontendServices: FrontendServices,
    val mainModuleDescriptor: ModuleDescriptor,
    exportedDependencies: Collection<ModuleDescriptor>,
) : SkiePhase.Context, InitPhase.Context by initPhaseContext {

    override val context: SkiePhase.Context
        get() = this

    override val descriptorProvider: MutableDescriptorProvider = NativeDescriptorProvider(
        exposedModules = setOf(mainModuleDescriptor) + exportedDependencies,
        konanConfig = konanConfig,
        frontendServices = frontendServices,
    )

    val declarationBuilder: DeclarationBuilderImpl = DeclarationBuilderImpl(mainModuleDescriptor, descriptorProvider)
}
