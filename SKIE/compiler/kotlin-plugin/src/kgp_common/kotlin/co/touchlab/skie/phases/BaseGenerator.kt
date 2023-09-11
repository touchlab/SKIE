package co.touchlab.skie.phases

import co.touchlab.skie.configuration.ConfigurationContainer

internal abstract class BaseGenerator(
    override val skieContext: SkieContext,
) : SkieCompilationPhase, ConfigurationContainer {

    protected val module: SkieModule
        get() = skieContext.module
}
