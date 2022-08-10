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

class SwiftKtObjectFilesPhase(
    private val originalPhase: CompilerPhase<CommonBackendContext, Unit, Unit>
): SameTypeCompilerPhase<CommonBackendContext, Unit> {
    override fun invoke(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext, input: Unit) {
        originalPhase.invoke(phaseConfig, phaserState, context, input)

        if (!context.configuration.getBoolean(ConfigurationKeys.isEnabled)) {
            return
        }

        val modules = context.configuration.getList(ConfigurationKeys.swiftPackModules)
        val swiftSources = context.configuration.getList(ConfigurationKeys.swiftSourceFiles)
        val expandedSwiftDir = context.configuration.getNotNull(ConfigurationKeys.expandedSwiftDir)
        val compilePhase = SwiftKtCompilePhase(modules, swiftSources, expandedSwiftDir)

        val config = context.javaClass.getMethod("getConfig").invoke(context) as KonanConfig
        val objCExport = context.javaClass.getMethod("getObjCExport").invoke(context)
        val namer = objCExport.javaClass.getField("namer").get(objCExport) as ObjCExportNamer?

        val swiftObjectFiles = compilePhase.process(config, context, namer ?: error("namer is null"))

        val compilerOutputField = context.javaClass.getField("compilerOutput")
        val originalCompilerOutput = compilerOutputField.get(context) as? List<ObjectFile>? ?: emptyList()
        compilerOutputField.set(context, originalCompilerOutput + swiftObjectFiles)
    }

    override val stickyPostconditions: Set<Checker<Unit>>
        get() = originalPhase.stickyPostconditions

    override fun getNamedSubphases(startDepth: Int): List<Pair<Int, NamedCompilerPhase<CommonBackendContext, *>>> {
        return originalPhase.getNamedSubphases(startDepth)
    }
}
