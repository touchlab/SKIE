@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.swiftlink.plugin

import co.touchlab.swiftlink.plugin.intercept.PhaseListener
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.backend.konan.Context as KonanContext

class SwiftLinkPhaseListener: PhaseListener {
    override val phase: PhaseListener.Phase = PhaseListener.Phase.OBJECT_FILES

    override fun afterPhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {
        super.afterPhase(phaseConfig, phaserState, context)

        if (context !is KonanContext) { return }

        val config = context.config
        val namer = context.objCExport.namer

        // We want to make sure that building new modules beyond this point fails, this is a hack to do that.
        SwiftPackModuleBuilder.Config.outputDir = null

        val swiftSources = context.configuration.getList(ConfigurationKeys.swiftSourceFiles)
        val expandedSwiftDir = context.configuration.getNotNull(ConfigurationKeys.expandedSwiftDir)
        val swiftObjectFiles = SwiftLinkCompilePhase(
            config,
            context,
            namer,
            swiftSources,
            expandedSwiftDir,
        ).process()

        context.compilerOutput += swiftObjectFiles
    }
}
