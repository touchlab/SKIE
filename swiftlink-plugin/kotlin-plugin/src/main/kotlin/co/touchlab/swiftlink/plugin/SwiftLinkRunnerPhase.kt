package co.touchlab.swiftlink.plugin

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.Checker
import org.jetbrains.kotlin.backend.common.phaser.CompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.NamedCompilerPhase
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.backend.common.phaser.SameTypeCompilerPhase
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.backend.konan.ObjectFile
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import kotlin.reflect.jvm.jvmName

private typealias ObjectFilePhase = CompilerPhase<CommonBackendContext, Unit, Unit>
private typealias CompilePhaseInvocation = (config: KonanConfig, context: CommonBackendContext, namer: ObjCExportNamer) -> List<ObjectFile>

class SwiftLinkRunnerPhase(
    private val originalPhase: ObjectFilePhase,
    private val compilePhaseInvocationKey: CompilerConfigurationKey<CompilePhaseInvocation>,
): SameTypeCompilerPhase<CommonBackendContext, Unit> {
    override fun invoke(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext, input: Unit) {
        originalPhase.invoke(phaseConfig, phaserState, context, input)

        val compilePhaseInvocation = context.configuration.get(compilePhaseInvocationKey) ?: return
        val config = context.javaClass.getMethod("getConfig").invoke(context) as KonanConfig
        val objCExport = context.javaClass.getMethod("getObjCExport").invoke(context)
        val namer = objCExport.javaClass.getField("namer").get(objCExport) as ObjCExportNamer?

        val swiftObjectFiles = compilePhaseInvocation(config, context, namer ?: error("namer is null"))

        val compilerOutputField = context.javaClass.getField("compilerOutput")
        val originalCompilerOutput = compilerOutputField.get(context) as? List<ObjectFile>? ?: emptyList()
        compilerOutputField.set(context, originalCompilerOutput + swiftObjectFiles)
    }

    override val stickyPostconditions: Set<Checker<Unit>>
        get() = originalPhase.stickyPostconditions

    override fun getNamedSubphases(startDepth: Int): List<Pair<Int, NamedCompilerPhase<CommonBackendContext, *>>> {
        return originalPhase.getNamedSubphases(startDepth)
    }

    fun accessInitialConfigForCopy(): Pair<ObjectFilePhase, CompilerConfigurationKey<CompilePhaseInvocation>> =
        originalPhase to compilePhaseInvocationKey

    companion object {
        fun register(configuration: CompilerConfiguration) {
            val phase = this::class.java.classLoader
                .loadClass("org.jetbrains.kotlin.backend.konan.ToplevelPhasesKt")
                .getDeclaredMethod("getObjectFilesPhase")
                .invoke(null)

            synchronized(phase) {
                val field = phase.javaClass
                    .getDeclaredField("lower")

                check(field.trySetAccessible()) { "Failed to make field `lower` accessible" }

                val currentPhase = field.get(phase) as CompilerPhase<CommonBackendContext, Unit, Unit>
                val (originalPhase, compilePhaseInvocationKey) = if (currentPhase.javaClass.name == SwiftLinkRunnerPhase::class.jvmName) {
                    currentPhase.javaClass.getDeclaredMethod(SwiftLinkRunnerPhase::accessInitialConfigForCopy.name)
                        .invoke(currentPhase) as Pair<ObjectFilePhase, CompilerConfigurationKey<CompilePhaseInvocation>>
                } else {
                    currentPhase to CompilerConfigurationKey.create("compilePhaseInvocation")
                }

                configuration.put(compilePhaseInvocationKey) { config, context, namer ->
                    val modules = context.configuration.getList(ConfigurationKeys.swiftPackModules)
                    val swiftSources = context.configuration.getList(ConfigurationKeys.swiftSourceFiles)
                    val expandedSwiftDir = context.configuration.getNotNull(ConfigurationKeys.expandedSwiftDir)
                    SwiftKtCompilePhase(modules, swiftSources, expandedSwiftDir).process(config, context, namer)
                }

                val swiftKtObjectFilesPhase = SwiftLinkRunnerPhase(originalPhase, compilePhaseInvocationKey)
                field.set(phase, swiftKtObjectFilesPhase)
            }
        }
    }
}
