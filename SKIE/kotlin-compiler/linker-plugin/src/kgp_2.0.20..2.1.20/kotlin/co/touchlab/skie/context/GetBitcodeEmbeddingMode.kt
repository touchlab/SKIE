package co.touchlab.skie.context

import co.touchlab.skie.configuration.SwiftCompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfiguration

actual fun CompilerConfiguration.getBitcodeEmbeddingMode(): SwiftCompilerConfiguration.BitcodeEmbeddingMode =
    SwiftCompilerConfiguration.BitcodeEmbeddingMode.None
