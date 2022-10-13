package co.touchlab.swiftgen.plugin.internal

import co.touchlab.swiftlink.plugin.intercept.PhaseListener
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState

internal class SwiftGenIrPhaseListener : PhaseListener {

    override val phase: PhaseListener.Phase = PhaseListener.Phase.PSI_TO_IR

    override fun beforePhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {
        super.beforePhase(phaseConfig, phaserState, context)

        val declarationBuilder = SwiftGenCompilerConfigurationKey.DeclarationBuilder.getOrNull(context.configuration)

        declarationBuilder?.suppressUnboundSymbolsError()
    }
}
