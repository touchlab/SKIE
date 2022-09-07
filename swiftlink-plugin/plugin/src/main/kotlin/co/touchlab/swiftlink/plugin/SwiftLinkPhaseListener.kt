package co.touchlab.swiftlink.plugin

import co.touchlab.swiftlink.plugin.intercept.PhaseListener
import com.google.auto.service.AutoService
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState

@AutoService(PhaseListener::class)
class SwiftLinkPhaseListener: PhaseListener {
    override val phase: PhaseListener.Phase = PhaseListener.Phase.OBJECT_FILES

    override fun afterPhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {
        super.afterPhase(phaseConfig, phaserState, context)

        val config = context.konanConfig
        val namer = context.objcExportNamer ?: error("namer is null")

        val modules = context.configuration.getList(ConfigurationKeys.swiftPackModules)
        val swiftSources = context.configuration.getList(ConfigurationKeys.swiftSourceFiles)
        val expandedSwiftDir = context.configuration.getNotNull(ConfigurationKeys.expandedSwiftDir)
        val swiftObjectFiles = SwiftKtCompilePhase(modules, swiftSources, expandedSwiftDir).process(config, context, namer)

        context.compilerOutput += swiftObjectFiles
    }
}
