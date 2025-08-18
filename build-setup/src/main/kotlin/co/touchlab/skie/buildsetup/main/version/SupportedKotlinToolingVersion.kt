package co.touchlab.skie.buildsetup.main.version

import co.touchlab.skie.gradle.KotlinToolingVersion

data class SupportedKotlinToolingVersion(
    val name: KotlinToolingVersion,
    // Can be different from name to allow for testing code of the given target against a different compiler version without having to rename everything.
    val primaryVersion: KotlinToolingVersion,
    val otherSupportedVersions: List<KotlinToolingVersion>,
) {

    val supportedVersions: List<KotlinToolingVersion> =
        listOf(primaryVersion) + otherSupportedVersions
}
