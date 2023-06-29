@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.intercept

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.CompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState

typealias InterceptedPhase<CONTEXT> = CompilerPhase<CONTEXT, Unit, Unit>
typealias ErasedListener = Pair<
        (phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) -> Unit,
        (phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) -> Unit
    >
