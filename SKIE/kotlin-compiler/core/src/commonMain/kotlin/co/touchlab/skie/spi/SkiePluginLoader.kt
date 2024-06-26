package co.touchlab.skie.spi

import co.touchlab.skie.phases.InitPhase
import java.util.ServiceLoader

class SkiePluginLoader {

    val pluginRegistrars: List<SkiePluginRegistrar> = ServiceLoader.load(
        SkiePluginRegistrar::class.java,
        SkiePluginRegistrar::class.java.classLoader
    ).toList()

    fun registerAll(initPhaseContext: InitPhase.Context) {
        pluginRegistrars.forEach {
            it.register(initPhaseContext)
        }
    }
}
