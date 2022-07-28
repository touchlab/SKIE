package co.touchlab.swiftkt.plugin

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
    private val originalPhase: CompilerPhase<CommonBackendContext, Unit, Unit>,
    private val swiftKtCompilePhase: SwiftKtCompilePhase,
    private val onInvokeCompleted: () -> Unit
): SameTypeCompilerPhase<CommonBackendContext, Unit> {
    override fun invoke(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext, input: Unit) = try {
        originalPhase.invoke(phaseConfig, phaserState, context, input)
        val config = context.javaClass.getMethod("getConfig").invoke(context) as KonanConfig
        val objCExport = context.javaClass.getMethod("getObjCExport").invoke(context)
        val namer = objCExport.javaClass.getField("namer").get(objCExport) as ObjCExportNamer?
        val swiftObjectFiles = swiftKtCompilePhase.process(config, context, namer ?: error("namer is null"))

        val compilerOutputField = context.javaClass.getField("compilerOutput")
        val originalCompilerOutput = compilerOutputField.get(context) as? List<ObjectFile>? ?: emptyList()
        compilerOutputField.set(context, originalCompilerOutput + swiftObjectFiles)
    } finally {
        onInvokeCompleted()
    }

    override val stickyPostconditions: Set<Checker<Unit>>
        get() = originalPhase.stickyPostconditions

    override fun getNamedSubphases(startDepth: Int): List<Pair<Int, NamedCompilerPhase<CommonBackendContext, *>>> {
        return originalPhase.getNamedSubphases(startDepth)
    }
}
