package co.touchlab.skie.plugin.generator.internal

import co.touchlab.skie.plugin.api.MutableDescriptorProviderKey
import co.touchlab.skie.plugin.api.skieContext
import co.touchlab.skie.plugin.generator.internal.util.NamespaceProvider
import co.touchlab.skie.plugin.generator.internal.util.NativeMutableDescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.Reporter
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.DeclarationBuilderImpl
import co.touchlab.skie.plugin.intercept.PhaseListener
import co.touchlab.skie.plugin.reflection.reflectors.ContextReflector
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.library.KLIB_PROPERTY_SHORT_NAME
import org.jetbrains.kotlin.library.shortName
import org.jetbrains.kotlin.library.uniqueName

internal class SkieSymbolTablePhaseListener : PhaseListener {

    override val phase: PhaseListener.Phase = PhaseListener.Phase.CREATE_SYMBOL_TABLE

    override fun beforePhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {
        val contextReflector = ContextReflector(context)
        contextReflector.librariesWithDependencies.forEach { library ->
            if (library.shortName == null) {
                library.manifestProperties.setProperty(
                    KLIB_PROPERTY_SHORT_NAME,
                    library.uniqueName.substringAfterLast(':'),
                )
            }
        }
    }

    override fun afterPhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {
        val descriptorProvider = NativeMutableDescriptorProvider(context)

        val declarationBuilder = DeclarationBuilderImpl(context, descriptorProvider)

        val skieScheduler = SkieCompilationScheduler(
            context = context,
            skieContext = context.skieContext,
            descriptorProvider = descriptorProvider,
            declarationBuilder = declarationBuilder,
            namespaceProvider = NamespaceProvider(context.skieContext.module),
            reporter = Reporter(context.configuration),
        )

        skieScheduler.runClassExportingPhases()

        descriptorProvider.reload()

        context.configuration.put(MutableDescriptorProviderKey, descriptorProvider)
        SkieCompilerConfigurationKey.SkieScheduler.put(skieScheduler, context.configuration)
    }
}
