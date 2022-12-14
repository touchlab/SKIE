@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.plugin.intercept

import co.touchlab.skie.plugin.reflection.PropertyField
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.Checker
import org.jetbrains.kotlin.backend.common.phaser.CompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.NamedCompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.backend.konan.objCExportPhase
import org.jetbrains.kotlin.backend.konan.objectFilesPhase
import org.jetbrains.kotlin.backend.konan.psiToIrPhase
import org.jetbrains.kotlin.backend.konan.createSymbolTablePhase
import org.jetbrains.kotlin.cli.jvm.plugins.ServiceLoaderLite
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import java.net.URLClassLoader
import java.util.*
import kotlin.reflect.jvm.jvmName

typealias InterceptedPhase<CONTEXT> = CompilerPhase<CONTEXT, Unit, Unit>
typealias ErasedListener = Pair<
        (phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) -> Unit,
        (phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) -> Unit
    >

class PhaseInterceptor<in CONTEXT : CommonBackendContext>(
    private val interceptedPhase: InterceptedPhase<CONTEXT>,
    private val listenersKey: CompilerConfigurationKey<List<ErasedListener>>,
) : CompilerPhase<CONTEXT, Unit, Unit> {

    override val stickyPostconditions: Set<Checker<Unit>>
        get() = interceptedPhase.stickyPostconditions

    override fun getNamedSubphases(startDepth: Int): List<Pair<Int, NamedCompilerPhase<CONTEXT, *>>> {
        return interceptedPhase.getNamedSubphases(startDepth)
    }

    override fun invoke(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CONTEXT, input: Unit) {
        val listeners = context.configuration.getList(listenersKey)

        listeners.forEach { it.first(phaseConfig, phaserState, context) }

        interceptedPhase.invoke(phaseConfig, phaserState, context, input)

        listeners.forEach { it.second(phaseConfig, phaserState, context) }
    }

    fun accessInitialConfigForCopy(): Pair<InterceptedPhase<CONTEXT>, CompilerConfigurationKey<List<ErasedListener>>> =
        interceptedPhase to listenersKey

    companion object {
        fun setupPhaseListeners(configuration: CompilerConfiguration) {
            val phaseListeners =
                (this::class.java.classLoader as? URLClassLoader)?.let { ServiceLoaderLite.loadImplementations<PhaseListener>(it) }
                    ?: ServiceLoader.load(PhaseListener::class.java)
            phaseListeners
                .groupBy { it.phase }
                .forEach { (phaseKey, interceptions) ->
                    val namedPhase = getPhase(phaseKey)

                    synchronized(namedPhase) {
                        val currentPhase = namedPhase.lowerField
                        val (originalPhase, listenersKey) = if (currentPhase.javaClass.name == PhaseInterceptor::class.jvmName) {
                            @Suppress("UNCHECKED_CAST")
                            currentPhase.javaClass.getDeclaredMethod(PhaseInterceptor<Nothing>::accessInitialConfigForCopy.name)
                                .invoke(currentPhase) as Pair<InterceptedPhase<Nothing>, CompilerConfigurationKey<List<ErasedListener>>>
                        } else {
                            currentPhase to CompilerConfigurationKey.create("phaseListeners")
                        }

                        // We need to get rid of the `PhaseListener` type as it's not available between different class loaders.
                        configuration.addAll(listenersKey, interceptions.map { it::beforePhase to it::afterPhase })

                        val interceptorPhase = PhaseInterceptor(originalPhase, listenersKey)
                        namedPhase.lowerField = interceptorPhase
                    }

                }
        }

        private fun getPhase(phaseKey: PhaseListener.Phase) = when (phaseKey) {
            PhaseListener.Phase.CREATE_SYMBOL_TABLE -> createSymbolTablePhase
            PhaseListener.Phase.OBJC_EXPORT -> objCExportPhase
            PhaseListener.Phase.PSI_TO_IR -> psiToIrPhase
            PhaseListener.Phase.OBJECT_FILES -> objectFilesPhase
        }

        private var NamedCompilerPhase<Nothing, Unit>.lowerField: CompilerPhase<Nothing, Unit, Unit> by PropertyField(NamedCompilerPhase<Nothing, Unit>::lower.name)
    }
}
