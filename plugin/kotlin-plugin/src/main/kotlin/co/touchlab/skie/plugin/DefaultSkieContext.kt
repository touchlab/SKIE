package co.touchlab.skie.plugin

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.SkieModule

class DefaultSkieContext(
    override val module: SkieModule,
) : SkieContext
