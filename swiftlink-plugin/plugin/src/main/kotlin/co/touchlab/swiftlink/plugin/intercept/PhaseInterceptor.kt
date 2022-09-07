package co.touchlab.swiftlink.plugin.intercept

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.Checker
import org.jetbrains.kotlin.backend.common.phaser.CompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.NamedCompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.cli.jvm.plugins.ServiceLoaderLite
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import java.net.URLClassLoader
import kotlin.reflect.jvm.jvmName

typealias InterceptedPhase = CompilerPhase<CommonBackendContext, Unit, Unit>
typealias ErasedListener = Pair<
        (phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) -> Unit,
        (phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) -> Unit
    >

class PhaseInterceptor(
    private val interceptedPhase: InterceptedPhase,
    private val listenersKey: CompilerConfigurationKey<List<ErasedListener>>,
): CompilerPhase<CommonBackendContext, Unit, Unit> {

    override val stickyPostconditions: Set<Checker<Unit>>
        get() = interceptedPhase.stickyPostconditions

    override fun getNamedSubphases(startDepth: Int): List<Pair<Int, NamedCompilerPhase<CommonBackendContext, *>>> {
        return interceptedPhase.getNamedSubphases(startDepth)
    }

    override fun invoke(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext, input: Unit) {
        val listeners = context.configuration.getList(listenersKey)

        listeners.forEach { it.first(phaseConfig, phaserState, context) }

        interceptedPhase.invoke(phaseConfig, phaserState, context, input)

        listeners.forEach { it.second(phaseConfig, phaserState, context) }
    }

    fun accessInitialConfigForCopy(): Pair<InterceptedPhase, CompilerConfigurationKey<List<ErasedListener>>> =
        interceptedPhase to listenersKey

    companion object {
        fun setupPhaseListeners(configuration: CompilerConfiguration) {
            ServiceLoaderLite.loadImplementations(PhaseListener::class.java, this::class.java.classLoader as URLClassLoader)
                .groupBy { it.phase }
                .forEach { (phaseKey, interceptions) ->
                    val phaseAccessor = when (phaseKey) {
                        PhaseListener.Phase.OBJC_EXPORT -> "getObjCExportPhase"
                        PhaseListener.Phase.OBJECT_FILES -> "getObjectFilesPhase"
                    }

                    val phase = this::class.java.classLoader
                        .loadClass("org.jetbrains.kotlin.backend.konan.ToplevelPhasesKt")
                        .getDeclaredMethod(phaseAccessor)
                        .invoke(null)

                    synchronized(phase) {
                        val field = phase.javaClass
                            .getDeclaredField("lower")

                        check(field.trySetAccessible()) { "Failed to make field `lower` accessible" }

                        val currentPhase = field.get(phase) as CompilerPhase<CommonBackendContext, Unit, Unit>
                        val (originalPhase, listenersKey) = if (currentPhase.javaClass.name == PhaseInterceptor::class.jvmName) {
                            currentPhase.javaClass.getDeclaredMethod(PhaseInterceptor::accessInitialConfigForCopy.name)
                                .invoke(currentPhase) as Pair<InterceptedPhase, CompilerConfigurationKey<List<ErasedListener>>>
                        } else {
                            currentPhase to CompilerConfigurationKey.create("phaseListeners")
                        }

                        // We need to get rid of the `PhaseListener` type as it's not available between different class loaders.
                        configuration.addAll(listenersKey, interceptions.map { it::beforePhase to it::afterPhase })

                        val interceptorPhase = PhaseInterceptor(originalPhase, listenersKey)
                        field.set(phase, interceptorPhase)
                    }

                }
        }
    }
}
