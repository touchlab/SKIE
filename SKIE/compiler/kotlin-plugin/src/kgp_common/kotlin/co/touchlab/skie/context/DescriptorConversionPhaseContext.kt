package co.touchlab.skie.context

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.builtin.KirBuiltins
import co.touchlab.skie.kir.descriptor.DescriptorKirProvider
import co.touchlab.skie.kir.descriptor.ObjCExportedInterfaceProvider
import co.touchlab.skie.kir.type.translation.KirCustomTypeMappers
import co.touchlab.skie.kir.type.translation.KirDeclarationTypeTranslator
import co.touchlab.skie.kir.type.translation.KirTypeTranslator
import co.touchlab.skie.phases.DescriptorConversionPhase
import co.touchlab.skie.phases.ForegroundPhase
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer

class DescriptorConversionPhaseContext(
    mainSkieContext: MainSkieContext,
    override val objCExportedInterfaceProvider: ObjCExportedInterfaceProvider,
) : DescriptorConversionPhase.Context, ForegroundPhase.Context by mainSkieContext {

    override val context: DescriptorConversionPhase.Context = this

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

    override val kirBuiltins: KirBuiltins = descriptorKirProvider.kirBuiltins

    private val kirCustomTypeMappers = KirCustomTypeMappers(kirBuiltins, lazy { kirTypeTranslator })

    override val kirTypeTranslator: KirTypeTranslator = KirTypeTranslator(descriptorKirProvider, kirCustomTypeMappers)

    override val kirDeclarationTypeTranslator: KirDeclarationTypeTranslator = KirDeclarationTypeTranslator(
        kirTypeTranslator = kirTypeTranslator,
        kirBuiltins = kirBuiltins,
    )

    override val namer: ObjCExportNamer
        get() = objCExportedInterfaceProvider.namer
}
