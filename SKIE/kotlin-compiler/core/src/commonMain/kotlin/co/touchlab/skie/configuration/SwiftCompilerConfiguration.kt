package co.touchlab.skie.configuration

import co.touchlab.skie.util.TargetTriple

data class SwiftCompilerConfiguration(
    val swiftVersion: String,
    val freeCompilerArgs: List<String>,
    val buildType: BuildType,
    val linkMode: LinkMode,
    val targetTriple: TargetTriple,
    val bitcodeEmbeddingMode: BitcodeEmbeddingMode,
    val absoluteSwiftcPath: String,
    val absoluteTargetSysRootPath: String,
    val osVersionMin: String,
) {

    enum class BuildType {
        Debug,
        Release,
    }

    enum class LinkMode {
        Dynamic,
        Static,
    }

    enum class BitcodeEmbeddingMode {
        None,
        Marker,
        Full,
    }
}
