package co.touchlab.swiftgen.plugin.internal

import co.touchlab.swiftgen.configuration.SwiftGenConfiguration
import co.touchlab.swiftgen.plugin.internal.util.DescriptorProvider
import co.touchlab.swiftgen.plugin.internal.util.FileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftlink.plugin.intercept.PhaseListener
import co.touchlab.swiftpack.api.buildSwiftPackModule
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState

internal class SwiftGenPhaseListener : PhaseListener {

    override val phase: PhaseListener.Phase = PhaseListener.Phase.OBJC_EXPORT

    override fun beforePhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {
        super.beforePhase(phaseConfig, phaserState, context)

        val compilerConfiguration = context.configuration
        val pluginConfiguration = SwiftGenCompilerConfiguration.Key.getOrNull(compilerConfiguration) ?: SwiftGenConfiguration()
        buildSwiftPackModule("SwiftGen") {
            val fileBuilderFactory = FileBuilderFactory()

            val swiftGenScheduler = SwiftGenScheduler(
                fileBuilderFactory = fileBuilderFactory,
                namespaceProvider = NamespaceProvider(fileBuilderFactory),
                configuration = pluginConfiguration,
                swiftPackModuleBuilder = this,
                reporter = Reporter(compilerConfiguration),
            )

            val descriptorProvider = DescriptorProvider(context)

            swiftGenScheduler.process(descriptorProvider)

            fileBuilderFactory.buildAll()
                .forEach { addFile(it) }
        }
    }
}
