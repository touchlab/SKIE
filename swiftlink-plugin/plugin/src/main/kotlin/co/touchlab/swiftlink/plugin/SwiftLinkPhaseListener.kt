package co.touchlab.swiftlink.plugin

import co.touchlab.swiftlink.plugin.intercept.PhaseListener
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import co.touchlab.swiftpack.spi.NamespacedSwiftPackModule
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

        // We want to make sure that building new modules beyond this point fails, this is a hack to do that.
        SwiftPackModuleBuilder.Config.outputDir = null

        val localModules = context.configuration.get(ConfigurationKeys.linkPhaseSwiftPackOutputDir)?.let {
            NamespacedSwiftPackModule.moduleReferencesInDir("link-phase", it)
        } ?: emptyList()

        val modules = context.configuration.getList(ConfigurationKeys.swiftPackModules) + localModules
        val swiftSources = context.configuration.getList(ConfigurationKeys.swiftSourceFiles)
        val expandedSwiftDir = context.configuration.getNotNull(ConfigurationKeys.expandedSwiftDir)
        val swiftObjectFiles = SwiftLinkCompilePhase(modules, swiftSources, expandedSwiftDir).process(config, context, namer)

        context.compilerOutput += swiftObjectFiles
    }
}
