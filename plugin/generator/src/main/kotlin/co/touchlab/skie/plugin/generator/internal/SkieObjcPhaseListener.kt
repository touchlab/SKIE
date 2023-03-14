package co.touchlab.skie.plugin.generator.internal

import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.generator.internal.util.NamespaceProvider
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import co.touchlab.skie.plugin.intercept.PhaseListener
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState

internal class SkieObjcPhaseListener : PhaseListener {

    override val phase: PhaseListener.Phase = PhaseListener.Phase.OBJC_EXPORT

    override fun beforePhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {
        super.beforePhase(phaseConfig, phaserState, context)

        val skieScheduler = SkieCompilationScheduler(
            skieContext = context.skieContext,
            descriptorProvider = context.skieDescriptorProvider,
            declarationBuilder = context.skieDeclarationBuilder,
            namespaceProvider = NamespaceProvider(context.skieContext.module),
            reporter = Reporter(context.configuration),
        )

        SkieCompilerConfigurationKey.SkieScheduler.put(skieScheduler, context.configuration)

        skieScheduler.runObjcPhases()
    }
}
