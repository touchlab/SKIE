package co.touchlab.skie.spi

import co.touchlab.skie.phases.InitPhase

interface SkiePluginRegistrar {

    fun register(initPhaseContext: InitPhase.Context)
}
