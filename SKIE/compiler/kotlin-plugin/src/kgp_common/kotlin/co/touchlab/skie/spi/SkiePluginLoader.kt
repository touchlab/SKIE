package co.touchlab.skie.spi

import co.touchlab.skie.phases.InitPhase
import java.util.ServiceLoader

object SkiePluginLoader {

    fun load(initPhaseContext: InitPhase.Context) {
        val loader = ServiceLoader.load(SkiePluginRegistrar::class.java)

        loader.forEach {
            it.register(initPhaseContext)
        }
    }
}
