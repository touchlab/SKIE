package co.touchlab.skie.spi

import co.touchlab.skie.configuration.ConfigurationKey
import co.touchlab.skie.phases.InitPhase

interface SkiePluginRegistrar {

    val customConfigurationKeys: Set<ConfigurationKey<*>>
        get() = emptySet()

    fun register(initPhaseContext: InitPhase.Context)
}
