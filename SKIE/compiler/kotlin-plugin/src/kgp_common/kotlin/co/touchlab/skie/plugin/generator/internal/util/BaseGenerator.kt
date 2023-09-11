package co.touchlab.skie.plugin.generator.internal.util

import co.touchlab.skie.plugin.api.SkieContext
import co.touchlab.skie.plugin.api.module.SkieModule
import co.touchlab.skie.plugin.generator.internal.configuration.ConfigurationContainer

internal abstract class BaseGenerator(
    override val skieContext: SkieContext,
) : SkieCompilationPhase, ConfigurationContainer {

    protected val module: SkieModule
        get() = skieContext.module
}
