package co.touchlab.skie.context

import co.touchlab.skie.configuration.SwiftCompilerConfiguration
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.backend.konan.BitcodeEmbedding

fun CompilerConfiguration.getBitcodeEmbeddingMode(): SwiftCompilerConfiguration.BitcodeEmbeddingMode {
    return when (this[KonanConfigKeys.BITCODE_EMBEDDING_MODE]) {
        BitcodeEmbedding.Mode.FULL -> SwiftCompilerConfiguration.BitcodeEmbeddingMode.Full
        BitcodeEmbedding.Mode.MARKER -> SwiftCompilerConfiguration.BitcodeEmbeddingMode.Marker
        BitcodeEmbedding.Mode.NONE, null -> SwiftCompilerConfiguration.BitcodeEmbeddingMode.None
    }
}
