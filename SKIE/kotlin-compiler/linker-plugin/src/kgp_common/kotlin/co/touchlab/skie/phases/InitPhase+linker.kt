package co.touchlab.skie.phases

import co.touchlab.skie.context.InitPhaseContext
import org.jetbrains.kotlin.config.CompilerConfiguration

val InitPhase.Context.compilerConfiguration: CompilerConfiguration
    get() = typedContext.compilerConfiguration

val InitPhase.Context.skiePhaseScheduler: SkiePhaseScheduler
    get() = typedContext.skiePhaseScheduler

private val InitPhase.Context.typedContext: InitPhaseContext
    get() = context as InitPhaseContext
