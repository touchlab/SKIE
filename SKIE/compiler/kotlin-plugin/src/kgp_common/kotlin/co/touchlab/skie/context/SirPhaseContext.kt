package co.touchlab.skie.context

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.builtin.KirBuiltins
import co.touchlab.skie.kir.type.translation.KirTypeTranslator
import co.touchlab.skie.oir.OirProvider
import co.touchlab.skie.oir.builtin.OirBuiltins
import co.touchlab.skie.oir.type.translation.OirCustomTypeMappers
import co.touchlab.skie.oir.type.translation.OirTypeTranslator
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.SkiePhase
import co.touchlab.skie.phases.oir.util.ExternalApiNotesProvider
import co.touchlab.skie.sir.SirProvider
import co.touchlab.skie.sir.SkieNamespaceProvider
import co.touchlab.skie.sir.builtin.SirBuiltins
import co.touchlab.skie.sir.type.translation.SirTypeTranslator
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer

class SirPhaseContext(
    mainSkieContext: MainSkieContext,
) : SirPhase.Context, SkiePhase.Context by mainSkieContext {

    override val context: SirPhase.Context = this

    override val namer: ObjCExportNamer = mainSkieContext.namer

    override val kirProvider: KirProvider = KirProvider(kotlinBuiltins, extraDescriptorBuiltins, namer)

    override val oirProvider: OirProvider = OirProvider(kirProvider.skieModule, extraDescriptorBuiltins, kirProvider, namer)

    override val sirProvider: SirProvider = SirProvider(framework, kirProvider, configurationProvider, skieConfiguration)

    override val kirBuiltins: KirBuiltins = kirProvider.kirBuiltins

    override val oirBuiltins: OirBuiltins = oirProvider.oirBuiltins

    override val sirBuiltins: SirBuiltins = sirProvider.sirBuiltins

    override val skieNamespaceProvider: SkieNamespaceProvider = SkieNamespaceProvider(
        kirProvider = kirProvider,
        sirProvider = sirProvider,
        mainModuleDescriptor = mainSkieContext.mainModuleDescriptor,
    )

    override val externalApiNotesProvider: ExternalApiNotesProvider = ExternalApiNotesProvider(
        sdkPath = configurables.absoluteTargetSysRoot,
        sirProvider = sirProvider,
    )

    override val kirTypeTranslator: KirTypeTranslator = KirTypeTranslator()

    override val oirTypeTranslator: OirTypeTranslator by lazy {
        OirTypeTranslator(
            kirProvider = kirProvider,
            oirProvider = oirProvider,
            oirBuiltins = oirBuiltins,
            customTypeMappers = OirCustomTypeMappers(
                kirBuiltins = kirBuiltins,
                oirBuiltins = oirBuiltins,
                translator = lazy {
                    oirTypeTranslator
                },
            ),
        )
    }

    override val sirTypeTranslator: SirTypeTranslator = SirTypeTranslator(sirBuiltins)
}
