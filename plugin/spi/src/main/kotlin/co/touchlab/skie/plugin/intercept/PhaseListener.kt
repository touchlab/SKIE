package co.touchlab.skie.plugin.intercept

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState

interface PhaseListener {
    val phase: Phase

    fun beforePhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {}

    fun afterPhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {}

    enum class Phase {
        OBJC_EXPORT,
        PSI_TO_IR,
        OBJECT_FILES,
    }
}
