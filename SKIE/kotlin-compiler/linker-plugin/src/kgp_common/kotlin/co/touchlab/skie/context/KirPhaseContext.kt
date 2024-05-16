package co.touchlab.skie.context

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.builtin.DescriptorBasedKirBuiltins
import co.touchlab.skie.kir.descriptor.DescriptorKirProvider
import co.touchlab.skie.kir.descriptor.ObjCExportedInterfaceProvider
import co.touchlab.skie.kir.type.translation.KirCustomTypeMappers
import co.touchlab.skie.kir.type.translation.KirDeclarationTypeTranslator
import co.touchlab.skie.kir.type.translation.KirTypeTranslator
import co.touchlab.skie.phases.KirPhase
import co.touchlab.skie.phases.extraDescriptorBuiltins
import co.touchlab.skie.phases.kotlinBuiltins
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer

class KirPhaseContext(
    mainSkieContext: MainSkieContext,
    val objCExportedInterfaceProvider: ObjCExportedInterfaceProvider,
) : KirPhase.Context, ForegroundPhaseCompilerContext by mainSkieContext {

    override val context: KirPhase.Context = this

    override val kirProvider: KirProvider = KirProvider(lazy { descriptorKirProvider }, globalConfiguration).also {
        mainSkieContext.kirProvider = it
    }

    val descriptorKirProvider: DescriptorKirProvider = DescriptorKirProvider(
        mainModuleDescriptor = mainSkieContext.mainModuleDescriptor,
        kirProvider = kirProvider,
        kotlinBuiltIns = kotlinBuiltins,
        extraDescriptorBuiltins = extraDescriptorBuiltins,
        namer = namer,
        descriptorConfigurationProvider = descriptorConfigurationProvider,
        globalConfiguration = globalConfiguration,
    ).also {
        mainSkieContext.descriptorKirProvider = it
    }

    override val kirBuiltins: DescriptorBasedKirBuiltins = descriptorKirProvider.kirBuiltins

    private val kirCustomTypeMappers = KirCustomTypeMappers(kirBuiltins, lazy { kirTypeTranslator })

    val kirTypeTranslator: KirTypeTranslator = KirTypeTranslator(descriptorKirProvider, kirCustomTypeMappers)

    val kirDeclarationTypeTranslator: KirDeclarationTypeTranslator = KirDeclarationTypeTranslator(
        kirTypeTranslator = kirTypeTranslator,
        kirBuiltins = kirBuiltins,
    )

    val namer: ObjCExportNamer
        get() = objCExportedInterfaceProvider.namer
}
