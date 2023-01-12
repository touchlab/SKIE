package co.touchlab.skie.plugin.generator.internal

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.generator.ConfigurationKeys
import co.touchlab.skie.plugin.generator.internal.util.NamespaceProvider
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import co.touchlab.skie.plugin.intercept.PhaseListener
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState

internal class SwiftGenObjcPhaseListener : PhaseListener {

    override val phase: PhaseListener.Phase = PhaseListener.Phase.OBJC_EXPORT

    override fun beforePhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {
        super.beforePhase(phaseConfig, phaserState, context)

        val skieScheduler = SkieScheduler(
            skieContext = context.skieContext,
            descriptorProvider = context.skieDescriptorProvider,
            declarationBuilder = context.skieDeclarationBuilder,
            namespaceProvider = NamespaceProvider(context.skieContext.module),
            configuration = context.pluginConfiguration,
            reporter = Reporter(context.configuration),
        )

        skieScheduler.process()
    }

    private val CommonBackendContext.pluginConfiguration: Configuration
        get() = configuration.get(ConfigurationKeys.swiftGenConfiguration, Configuration {})
}
