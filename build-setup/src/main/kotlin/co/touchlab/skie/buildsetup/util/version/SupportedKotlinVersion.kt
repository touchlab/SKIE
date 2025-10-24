package co.touchlab.skie.buildsetup.util.version

data class SupportedKotlinVersion(
    val name: KotlinToolingVersion,
    // Can be different from name to allow for testing code of the given target against a different compiler version without having to rename everything.
    val compilerVersion: KotlinToolingVersion,
    val otherSupportedVersions: List<KotlinToolingVersion>,
) {

    val supportedVersions: List<KotlinToolingVersion> =
        listOf(compilerVersion) + otherSupportedVersions
}
