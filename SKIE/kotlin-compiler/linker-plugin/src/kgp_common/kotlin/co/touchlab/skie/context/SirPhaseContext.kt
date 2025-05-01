package co.touchlab.skie.context

import co.touchlab.skie.kir.KirProvider
import co.touchlab.skie.kir.builtin.KirBuiltins
import co.touchlab.skie.oir.OirProvider
import co.touchlab.skie.oir.type.translation.OirTypeTranslator
import co.touchlab.skie.phases.BackgroundPhase
import co.touchlab.skie.phases.SirPhase
import co.touchlab.skie.phases.oir.util.ExternalApiNotesProvider
import co.touchlab.skie.sir.NamespaceProvider
import co.touchlab.skie.sir.SirFileProvider
import co.touchlab.skie.sir.SirProvider
import co.touchlab.skie.sir.builtin.SirBuiltins
import co.touchlab.skie.sir.type.translation.SirTypeTranslator

class SirPhaseContext(mainSkieContext: MainSkieContext) :
    SirPhase.Context,
    BackgroundPhase.Context by mainSkieContext {

    override val context: SirPhase.Context = this

    override val kirProvider: KirProvider = mainSkieContext.kirProvider

    override val oirProvider: OirProvider = OirProvider(kirProvider)

    override val sirProvider: SirProvider = SirProvider(framework, oirProvider, skieBuildDirectory, globalConfiguration)

    override val sirFileProvider: SirFileProvider = sirProvider.fileProvider

    override val kirBuiltins: KirBuiltins = kirProvider.kirBuiltins

    override val sirBuiltins: SirBuiltins = sirProvider.sirBuiltins

    override val namespaceProvider: NamespaceProvider = NamespaceProvider(
        kirProvider = kirProvider,
        sirProvider = sirProvider,
        sirFileProvider = sirFileProvider,
    )

    override val externalApiNotesProvider: ExternalApiNotesProvider = ExternalApiNotesProvider(
        sdkPath = swiftCompilerConfiguration.absoluteTargetSysRootPath,
        sirProvider = sirProvider,
    )

    override val oirTypeTranslator: OirTypeTranslator = OirTypeTranslator()

    override val sirTypeTranslator: SirTypeTranslator = SirTypeTranslator(sirBuiltins)
}
