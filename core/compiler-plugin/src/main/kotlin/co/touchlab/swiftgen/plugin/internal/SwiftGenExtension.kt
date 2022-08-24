package co.touchlab.swiftgen.plugin.internal

import co.touchlab.swiftgen.configuration.SwiftGenConfiguration
import co.touchlab.swiftgen.plugin.internal.util.FileBuilderFactory
import co.touchlab.swiftgen.plugin.internal.util.NamespaceProvider
import co.touchlab.swiftgen.plugin.internal.util.Reporter
import co.touchlab.swiftpack.api.buildSwiftPackModule
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class SwiftGenExtension(
    private val pluginConfiguration: SwiftGenConfiguration,
    private val compilerConfiguration: CompilerConfiguration,
) : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        buildSwiftPackModule("SwiftGen") {
            val fileBuilderFactory = FileBuilderFactory()

            val swiftGenScheduler = SwiftGenScheduler(
                fileBuilderFactory = fileBuilderFactory,
                namespaceProvider = NamespaceProvider(fileBuilderFactory, moduleFragment),
                configuration = pluginConfiguration,
                reporter = Reporter(compilerConfiguration),
            )

            swiftGenScheduler.process(moduleFragment)

            fileBuilderFactory.buildAll()
                .forEach { addFile(it) }
        }
    }
}
