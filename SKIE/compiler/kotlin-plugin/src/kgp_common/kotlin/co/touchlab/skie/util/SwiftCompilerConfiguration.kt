package co.touchlab.skie.util

import org.jetbrains.kotlin.backend.konan.BitcodeEmbedding
import org.jetbrains.kotlin.konan.target.TargetTriple

data class SwiftCompilerConfiguration(
    val swiftVersion: String,
    val additionalFlags: List<String>,
    val buildType: BuildType,
    val targetTriple: TargetTriple,
    val bitcodeEmbeddingMode: BitcodeEmbedding.Mode,
    val absoluteTargetToolchainPath: String,
    val absoluteTargetSysRootPath: String,
    val osVersionMin: String,
) {

    enum class BuildType {
        Debug, Release
    }
}
