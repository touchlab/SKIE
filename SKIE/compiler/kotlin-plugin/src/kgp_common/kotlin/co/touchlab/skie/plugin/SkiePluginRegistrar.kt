package co.touchlab.skie.plugin

import co.touchlab.skie.context.MainSkieContext

interface SkiePluginRegistrar {

    fun register(mainSkieContext: MainSkieContext)
}
