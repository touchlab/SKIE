package co.touchlab.skie.context

import co.touchlab.skie.configuration.SwiftCompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfiguration

fun CompilerConfiguration.getBitcodeEmbeddingMode(): SwiftCompilerConfiguration.BitcodeEmbeddingMode {
    return SwiftCompilerConfiguration.BitcodeEmbeddingMode.None
}
