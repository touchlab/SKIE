package co.touchlab.skie.phases.other

import co.touchlab.skie.configuration.SwiftCompilerConfiguration
import co.touchlab.skie.phases.ClassExportPhase

object VerifyNoBitcodeEmbeddingPhase : ClassExportPhase {

    context(ClassExportPhase.Context)
    override suspend fun execute() {
        if (swiftCompilerConfiguration.bitcodeEmbeddingMode == SwiftCompilerConfiguration.BitcodeEmbeddingMode.Full) {
            error(
                "Bitcode embedding is not supported by SKIE. " +
                    "To disable bitcode embedding you likely need to remove `embedBitcode(BitcodeEmbeddingMode.BITCODE)` from the Gradle build script.",
            )
        }
    }
}
