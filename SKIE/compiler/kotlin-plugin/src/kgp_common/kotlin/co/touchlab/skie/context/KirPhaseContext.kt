package co.touchlab.skie.context

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.builtin.DescriptorBasedKirBuiltins
import co.touchlab.skie.kir.descriptor.DescriptorKirProvider
import co.touchlab.skie.kir.descriptor.ObjCExportedInterfaceProvider
import co.touchlab.skie.kir.type.translation.KirCustomTypeMappers
import co.touchlab.skie.kir.type.translation.KirDeclarationTypeTranslator
import co.touchlab.skie.kir.type.translation.KirTypeTranslator
import co.touchlab.skie.phases.ForegroundCompilerPhase
import co.touchlab.skie.phases.KirCompilerPhase
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer

class KirPhaseContext(
    mainSkieContext: MainSkieContext,
    override val objCExportedInterfaceProvider: ObjCExportedInterfaceProvider,
) : KirCompilerPhase.Context, ForegroundCompilerPhase.Context by mainSkieContext {

    override val context: KirCompilerPhase.Context = this

    override val kirProvider: KirProvider = KirProvider(lazy { descriptorKirProvider }, rootConfiguration).also {
        mainSkieContext.kirProvider = it
    }

    override val descriptorKirProvider: DescriptorKirProvider = DescriptorKirProvider(
        mainModuleDescriptor = mainSkieContext.mainModuleDescriptor,
        kirProvider = kirProvider,
        kotlinBuiltIns = kotlinBuiltins,
        extraDescriptorBuiltins = extraDescriptorBuiltins,
        namer = namer,
        descriptorConfigurationProvider = descriptorConfigurationProvider,
        rootConfiguration = rootConfiguration,
    ).also {
        mainSkieContext.descriptorKirProvider = it
    }

    override val kirBuiltins: DescriptorBasedKirBuiltins = descriptorKirProvider.kirBuiltins

    private val kirCustomTypeMappers = KirCustomTypeMappers(kirBuiltins, lazy { kirTypeTranslator })

    override val kirTypeTranslator: KirTypeTranslator = KirTypeTranslator(descriptorKirProvider, kirCustomTypeMappers)

    override val kirDeclarationTypeTranslator: KirDeclarationTypeTranslator = KirDeclarationTypeTranslator(
        kirTypeTranslator = kirTypeTranslator,
        kirBuiltins = kirBuiltins,
    )

    override val namer: ObjCExportNamer
        get() = objCExportedInterfaceProvider.namer
}
