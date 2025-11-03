package co.touchlab.skie.entrypoint

import co.touchlab.skie.compilerinject.compilerplugin.mainSkieContext
import co.touchlab.skie.context.KotlinIrPhaseContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContextImpl
import org.jetbrains.kotlin.backend.konan.serialization.KonanIrLinker
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar.ExtensionStorage
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.util.SymbolTable

class SkieIrGenerationExtension(private val configuration: CompilerConfiguration) : IrGenerationExtension {

    @Suppress("DEPRECATION")
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        EntrypointUtils.runKotlinIrPhases(
            mainSkieContext = configuration.mainSkieContext,
            moduleFragment = moduleFragment,
            pluginContext = KotlinIrPhaseContext.CompatibleIrPluginContext(
                symbolTable = pluginContext.symbolTable as SymbolTable, pluginContext.irBuiltIns,
                bindingContext = pluginContext.bindingContext,
                linker = ((pluginContext as? IrPluginContextImpl)?.linker as KonanIrLinker),
                languageVersionSettings = pluginContext.languageVersionSettings,
                typeTranslator = pluginContext.typeTranslator,
            ),
        )
    }
}

fun ExtensionStorage.registerSkieIrGenerationExtensionIfNeeded(configuration: CompilerConfiguration) {
    IrGenerationExtension.registerExtension(SkieIrGenerationExtension(configuration))
}
