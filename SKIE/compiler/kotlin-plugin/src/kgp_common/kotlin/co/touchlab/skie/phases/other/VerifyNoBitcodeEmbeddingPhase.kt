package co.touchlab.skie.phases.other

import co.touchlab.skie.phases.ClassExportPhase
import org.jetbrains.kotlin.backend.konan.BitcodeEmbedding

object VerifyNoBitcodeEmbeddingPhase : ClassExportPhase {

    context(ClassExportPhase.Context)
    override suspend fun execute() {
        if (swiftCompilerConfiguration.bitcodeEmbeddingMode == BitcodeEmbedding.Mode.FULL) {
            error(
                "Bitcode embedding is not supported by SKIE. " +
                    "To disable bitcode embedding you likely need to remove `embedBitcode(BitcodeEmbeddingMode.BITCODE)` from the Gradle build script.",
            )
        }
    }
}
