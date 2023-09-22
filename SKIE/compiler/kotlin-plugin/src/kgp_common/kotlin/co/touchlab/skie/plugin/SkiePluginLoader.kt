package co.touchlab.skie.plugin

import co.touchlab.skie.context.MainSkieContext
import java.util.ServiceLoader

object SkiePluginLoader {

    fun load(mainSkieContext: MainSkieContext) {
        val loader = ServiceLoader.load(SkiePluginRegistrar::class.java)

        loader.forEach {
            it.register(mainSkieContext)
        }
    }
}
