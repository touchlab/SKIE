package co.touchlab.skie.compilerinject.interceptor

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.backend.common.phaser.SameTypeCompilerPhase
import org.jetbrains.kotlin.config.CompilerConfigurationKey

internal class InterceptedSameTypeCompilerPhase<Context: CommonBackendContext, Data>(
    val originalPhase: SameTypeCompilerPhase<Context, Data>,
    val interceptorKey: CompilerConfigurationKey<ErasedPhaseInterceptor<Context, Data, Data>>,
): SameTypeCompilerPhase<Context, Data> {

    override fun invoke(phaseConfig: PhaseConfig, phaserState: PhaserState<Data>, context: Context, input: Data): Data {
        val interceptor = context.configuration.get(interceptorKey)
        return if (interceptor != null) {
            interceptor.invoke(context, input) { innerContext, innerInput ->
                originalPhase.invoke(phaseConfig, phaserState, innerContext, innerInput)
            }
        } else {
            originalPhase.invoke(phaseConfig, phaserState, context, input)
        }
    }
}
