package co.touchlab.skie.spi

import co.touchlab.skie.context.MainSkieContext

interface SkiePluginRegistrar {

    fun register(mainSkieContext: MainSkieContext)
}
