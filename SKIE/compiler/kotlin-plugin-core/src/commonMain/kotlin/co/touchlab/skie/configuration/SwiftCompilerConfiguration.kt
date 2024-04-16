package co.touchlab.skie.configuration

data class SwiftCompilerConfiguration(
    val swiftVersion: String,
    val additionalFlags: List<String>,
    val buildType: BuildType,
    val targetTriple: TargetTriple,
    val bitcodeEmbeddingMode: BitcodeEmbeddingMode,
    val absoluteTargetToolchainPath: String,
    val absoluteTargetSysRootPath: String,
    val osVersionMin: String,
) {

    enum class BuildType {
        Debug, Release
    }

    enum class BitcodeEmbeddingMode {
        None, Marker, Full
    }

    data class TargetTriple(
        val architecture: String,
        val vendor: String,
        val os: String,
        val environment: String?,
    ) {

        override fun toString(): String {
            val envSuffix = environment?.let { "-$environment" } ?: ""

            return "$architecture-$vendor-$os$envSuffix"
        }

        fun withOsVersion(osVersion: String): TargetTriple =
            copy(os = "$os$osVersion")
    }
}
