package co.touchlab.skie.entrypoint

import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar.ExtensionStorage

@Suppress("unused")
fun ExtensionStorage.registerSkieIrGenerationExtensionIfNeeded(configuration: CompilerConfiguration) {
    // IrGenerationExtensions are no longer called in 2.2.20.
}
