package co.touchlab.skie.phases

import co.touchlab.skie.context.InitPhaseContext
import co.touchlab.skie.util.DescriptorReporter
import org.jetbrains.kotlin.config.CompilerConfiguration

val InitPhase.Context.compilerConfiguration: CompilerConfiguration
    get() = typedContext.compilerConfiguration

val InitPhase.Context.descriptorReporter: DescriptorReporter
    get() = typedContext.descriptorReporter

private val InitPhase.Context.typedContext: InitPhaseContext
    get() = context as InitPhaseContext
