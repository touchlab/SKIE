@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.intercept

import co.touchlab.skie.plugin.reflection.Reflector
import org.jetbrains.kotlin.backend.common.LoggingContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfigurationService
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.backend.common.phaser.SameTypeCompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.SameTypeNamedCompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.SimpleNamedCompilerPhase
import org.jetbrains.kotlin.backend.konan.ConfigChecks
// import org.jetbrains.kotlin.backend.konan.createSymbolTablePhase
// import org.jetbrains.kotlin.backend.konan.objCExportPhase
import org.jetbrains.kotlin.backend.konan.driver.phases.PsiToIrPhase
import org.jetbrains.kotlin.backend.konan.driver.phases.ProduceObjCExportInterfacePhase
import org.jetbrains.kotlin.backend.konan.driver.phases.CreateObjCFrameworkPhase
import org.jetbrains.kotlin.backend.konan.driver.phases.CreateObjCExportCodeSpecPhase
import org.jetbrains.kotlin.backend.konan.objectFilesPhase
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.util.ServiceLoaderLite
import java.net.URLClassLoader
import java.util.ServiceLoader
import kotlin.reflect.jvm.jvmName

typealias OriginalPhaseBody<Context, Input, Output> = (Context, Input) -> Output
typealias ErasedPhaseInterceptor<Context, Input, Output> = (Context, Input, OriginalPhaseBody<Context, Input, Output>) -> Output

object PhaseInterceptorRegistrar {
    fun setupPhaseInterceptors(configuration: CompilerConfiguration) {
        val phaseInterceptors =
            (this::class.java.classLoader as? URLClassLoader)?.let { ServiceLoaderLite.loadImplementations(it) }
                ?: ServiceLoader.load(PhaseInterceptor::class.java)

        phaseInterceptors
            .groupBy { it.phase }
            .forEach { (phase, interceptors) ->
                @Suppress("UNCHECKED_CAST")
                when (phase) {
                    is PhaseInterceptor.Phase.SameTypeNamed -> setupSameTypeNamedPhaseInterceptors(
                        configuration,
                        phase as PhaseInterceptor.Phase.SameTypeNamed<Nothing, Any?>,
                        interceptors as List<PhaseInterceptor<Nothing, Any?, Any?>>,
                    )
                    is PhaseInterceptor.Phase.SimpleNamed -> setupSimpleNamedPhaseInterceptors(
                        configuration,
                        phase as PhaseInterceptor.Phase.SimpleNamed<Nothing, Any?, Any?>,
                        interceptors as List<PhaseInterceptor<Nothing, Any?, Any?>>,
                    )
                }
            }
    }

    private fun <Context, Data> setupSameTypeNamedPhaseInterceptors(
        configuration: CompilerConfiguration,
        phase: PhaseInterceptor.Phase.SameTypeNamed<Context, Data>,
        interceptors: List<PhaseInterceptor<Context, Data, Data>>,
    ) where Context: LoggingContext, Context: ConfigChecks {
        val namedPhase = phase.reflector
        val chain = ErasedPhaseInterceptorChain(interceptors)

        synchronized(phase.kotlinPhase) {
            val currentPhase = namedPhase.lower
            val (originalPhase, interceptorKey) = if (currentPhase.isIntercepted()) {
                val interceptedPhase = InterceptedSameTypeCompilerPhaseReflector(currentPhase)
                interceptedPhase.originalPhase to interceptedPhase.interceptorKey
            } else {
                currentPhase to CompilerConfigurationKey.create("phaseInterceptor for phase $phase")
            }

            configuration.put(interceptorKey, chain)

            val interceptorPhase = InterceptedSameTypeCompilerPhase(originalPhase, interceptorKey)
            namedPhase.lower = interceptorPhase
        }
    }

    private fun <Context, Input, Output> setupSimpleNamedPhaseInterceptors(
        configuration: CompilerConfiguration,
        phase: PhaseInterceptor.Phase.SimpleNamed<Context, Input, Output>,
        interceptors: List<PhaseInterceptor<Context, Input, Output>>,
    ) where Context: LoggingContext, Context: ConfigChecks {
        val namedPhase = phase.reflector
        val chain = ErasedPhaseInterceptorChain(interceptors)
        synchronized(phase.kotlinPhase) {
            val currentPhaseBody = namedPhase.phaseBody
            val (originalPhaseBody, interceptorKey) = if (currentPhaseBody.isIntercepted()) {
                val interceptedPhaseBody = InterceptedPhaseBodyReflector(currentPhaseBody)
                interceptedPhaseBody.originalPhaseBody to interceptedPhaseBody.interceptorKey
            } else {
                currentPhaseBody to CompilerConfigurationKey.create("phaseInterceptor for phase $phase")
            }

            configuration.put(interceptorKey, chain)

            val interceptedPhaseBody = InterceptedPhaseBody(
                originalPhaseBody,
                interceptorKey,
            )
            namedPhase.phaseBody = interceptedPhaseBody
        }
    }

    @Suppress("UNCHECKED_CAST")
    private val <Context, Data> PhaseInterceptor.Phase.SameTypeNamed<Context, Data>.kotlinPhase: SameTypeNamedCompilerPhase<Context, Data> where Context: LoggingContext, Context: ConfigChecks
        get() = when (this) {
            PhaseInterceptor.Phase.ObjectFiles -> objectFilesPhase
        } as SameTypeNamedCompilerPhase<Context, Data>

    private val <Context, Data> PhaseInterceptor.Phase.SameTypeNamed<Context, Data>.reflector: SameTypeNamedCompilerPhaseReflector<Context, Data> where Context: LoggingContext, Context: ConfigChecks
        get() = SameTypeNamedCompilerPhaseReflector(kotlinPhase)

    @Suppress("UNCHECKED_CAST")
    private val <Context, Input, Output> PhaseInterceptor.Phase.SimpleNamed<Context, Input, Output>.kotlinPhase: SimpleNamedCompilerPhase<Context, Input, Output> where Context: LoggingContext, Context: ConfigChecks
        get() = when (this) {
            PhaseInterceptor.Phase.CreateObjCExportCodeSpec -> CreateObjCExportCodeSpecPhase
            PhaseInterceptor.Phase.CreateObjCFramework -> CreateObjCFrameworkPhase
            PhaseInterceptor.Phase.ProduceObjCExportInterface -> ProduceObjCExportInterfacePhase
            PhaseInterceptor.Phase.PsiToIr -> PsiToIrPhase
        } as SimpleNamedCompilerPhase<Context, Input, Output>

    private val <Context, Input, Output> PhaseInterceptor.Phase.SimpleNamed<Context, Input, Output>.reflector: SimpleNamedCompilerPhaseReflector<Context, Input, Output> where Context: LoggingContext, Context: ConfigChecks
        get() = SimpleNamedCompilerPhaseReflector(kotlinPhase)
}

infix fun <Context, Input, Output> ErasedPhaseInterceptor<Context, Input, Output>.then(
    next: ErasedPhaseInterceptor<Context, Input, Output>,
): ErasedPhaseInterceptor<Context, Input, Output> where Context: LoggingContext, Context: ConfigChecks {
    return { outerContext, outerInput, original ->
        this.invoke(outerContext, outerInput) { innerContext, innerInput ->
            next.invoke(innerContext, innerInput, original)
        }
    }
}

private class InterceptedSameTypeCompilerPhase<Context, Data>(
    val originalPhase: SameTypeCompilerPhase<Context, Data>,
    val interceptorKey: CompilerConfigurationKey<ErasedPhaseInterceptor<Context, Data, Data>>,
): SameTypeCompilerPhase<Context, Data> where Context: LoggingContext, Context: ConfigChecks {
    override fun invoke(phaseConfig: PhaseConfigurationService, phaserState: PhaserState<Data>, context: Context, input: Data): Data {
        val interceptor = context.config.configuration.get(interceptorKey)
        return if (interceptor != null) {
            interceptor.invoke(context, input) { innerContext, innerInput ->
                originalPhase.invoke(phaseConfig, phaserState, innerContext, innerInput)
            }
        } else {
            originalPhase.invoke(phaseConfig, phaserState, context, input)
        }
    }
}

private class InterceptedPhaseBody<Context, Input, Output>(
    val originalPhaseBody: OriginalPhaseBody<Context, Input, Output>,
    val interceptorKey: CompilerConfigurationKey<ErasedPhaseInterceptor<Context, Input, Output>>,
): (Context, Input) -> Output where Context: LoggingContext, Context: ConfigChecks {
    override fun invoke(context: Context, input: Input): Output {
        val interceptor = context.config.configuration.get(interceptorKey)
        return if (interceptor != null) {
            interceptor.invoke(context, input, originalPhaseBody)
        } else {
            originalPhaseBody.invoke(context, input)
        }
    }
}

private class ErasedPhaseInterceptorChain<Context, Input, Output>(
    interceptors: List<PhaseInterceptor<Context, Input, Output>>,
): ErasedPhaseInterceptor<Context, Input, Output> where Context: LoggingContext, Context: ConfigChecks {
    // We need to get rid of the `PhaseInterceptor` type as it's not available between different class loaders
    private val chainedInterceptors: ErasedPhaseInterceptor<Context, Input, Output> by lazy {
        val erasedInterceptors: Sequence<ErasedPhaseInterceptor<Context, Input, Output>> = interceptors.asSequence().map { it::intercept }
        erasedInterceptors.reduce { acc, next ->
            acc then next
        }
    }

    override fun invoke(context: Context, input: Input, original: OriginalPhaseBody<Context, Input, Output>): Output {
        return chainedInterceptors(context, input, original)
    }
}

private class SameTypeNamedCompilerPhaseReflector<Context, Data>(
    override val instance: SameTypeNamedCompilerPhase<Context, Data>,
): Reflector(instance::class) where Context: LoggingContext, Context: ConfigChecks {
    var lower: SameTypeCompilerPhase<Context, Data> by declaredField()
}

private class SimpleNamedCompilerPhaseReflector<Context, Input, Output>(
    override val instance: SimpleNamedCompilerPhase<Context, Input, Output>,
): Reflector(instance::class) where Context: LoggingContext, Context: ConfigChecks {
    var phaseBody: (Context, Input) -> Output by declaredField("\$phaseBody")
}

private class InterceptedPhaseBodyReflector<Context, Input, Output>(
    override val instance: (Context, Input) -> Output,
): Reflector(instance::class) where Context: LoggingContext, Context: ConfigChecks {
    val originalPhaseBody: (Context, Input) -> Output by declaredField()
    val interceptorKey: CompilerConfigurationKey<ErasedPhaseInterceptor<Context, Input, Output>> by declaredField()
}

private class InterceptedSameTypeCompilerPhaseReflector<Context, Data>(
    override val instance: SameTypeCompilerPhase<Context, Data>,
): Reflector(instance::class) where Context: LoggingContext, Context: ConfigChecks {
    val originalPhase: SameTypeCompilerPhase<Context, Data> by declaredField()
    val interceptorKey: CompilerConfigurationKey<ErasedPhaseInterceptor<Context, Data, Data>> by declaredField()
}

private fun <Context, Data> SameTypeCompilerPhase<Context, Data>.isIntercepted(): Boolean where Context: LoggingContext, Context: ConfigChecks {
    return javaClass.name == InterceptedSameTypeCompilerPhase::class.jvmName
}

private fun <Context, Input, Output> ((Context, Input) -> Output).isIntercepted(): Boolean where Context: LoggingContext, Context: ConfigChecks {
    return javaClass.name == InterceptedPhaseBody::class.jvmName
}
