package co.touchlab.swiftgen.plugin.internal

import co.touchlab.swiftgen.configuration.SwiftGenConfiguration
import co.touchlab.swiftgen.plugin.internal.util.FileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftlink.plugin.getAllExportedModuleDescriptors
import co.touchlab.swiftlink.plugin.intercept.PhaseListener
import co.touchlab.swiftpack.api.buildSwiftPackModule
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.phaser.PhaseConfig
import org.jetbrains.kotlin.backend.common.phaser.PhaserState
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class SwiftGenIrGenerationExtension(
    private val pluginConfiguration: SwiftGenConfiguration,
    private val compilerConfiguration: CompilerConfiguration,
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        buildSwiftPackModule("SwiftGen") {
            val fileBuilderFactory = FileBuilderFactory()

            val descriptor = moduleFragment.descriptor

            val swiftGenScheduler = SwiftGenScheduler(
                fileBuilderFactory = fileBuilderFactory,
                namespaceProvider = NamespaceProvider(fileBuilderFactory, descriptor),
                configuration = pluginConfiguration,
                swiftPackModuleBuilder = this,
                reporter = Reporter(compilerConfiguration),
            )

            swiftGenScheduler.process(descriptor)

            fileBuilderFactory.buildAll()
                .forEach { addFile(it) }
        }
    }
}

val swiftGenConfigurationKey = CompilerConfigurationKey<SwiftGenConfiguration>("SwiftGen configuration")

internal class SwiftGenPhaseListener: PhaseListener {
    override val phase: PhaseListener.Phase = PhaseListener.Phase.OBJC_EXPORT

    override fun beforePhase(phaseConfig: PhaseConfig, phaserState: PhaserState<Unit>, context: CommonBackendContext) {
        super.beforePhase(phaseConfig, phaserState, context)

        val compilerConfiguration = context.configuration
        val pluginConfiguration = SwiftGenCompilerConfiguration.Key.getOrNull(compilerConfiguration) ?: SwiftGenConfiguration()
        buildSwiftPackModule("SwiftGen") {
            val fileBuilderFactory = FileBuilderFactory()

            context.getAllExportedModuleDescriptors().forEach { descriptor ->
                val swiftGenScheduler = SwiftGenScheduler(
                    fileBuilderFactory = fileBuilderFactory,
                    namespaceProvider = NamespaceProvider(fileBuilderFactory, descriptor),
                    configuration = pluginConfiguration,
                    swiftPackModuleBuilder = this,
                    reporter = Reporter(compilerConfiguration),
                )

                swiftGenScheduler.process(descriptor)

                fileBuilderFactory.buildAll()
                    .forEach { addFile(it) }
            }
        }
    }
}
