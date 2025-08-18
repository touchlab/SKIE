package co.touchlab.skie.buildsetup.version

import org.gradle.api.Project

object KotlinToolingVersionProvider {

    fun getActiveKotlinToolingVersion(project: Project): KotlinToolingVersion {
        val activeVersion = project.property("versionSupport.kotlinTooling.activeVersion").toString()

        return KotlinToolingVersion(activeVersion)
    }

    /**
     * Supported format:
     * ```
     * versionSupport.kotlinTooling=1.8.0, 1.8.20(1.8.21, 1.8.22), 1.9.0[1.9.0-RC](1.9.10), 1.9.20[1.9.20-RC]
     * ```
     *
     * The first version is used as the name of the component and as the primary version.
     * The [] parentheses can be used to override the primary version, which can be useful for new version development
     * against and RC version when the full version is not available yet.
     * Otherwise, we would have to rename everything once the full version is released.
     *
     * The () parentheses can be used to specify other supported versions (the primary version is supported by default).
     */
    fun getSupportedKotlinToolingVersions(project: Project): List<SupportedKotlinToolingVersion> {
        val kotlinToolingProperty = project.property("versionSupport.kotlinTooling").toString()

        val versions = splitVersions(kotlinToolingProperty)

        return versions.map { parseVersion(it) }
    }

    private fun splitVersions(kotlinToolingProperty: String): List<String> {
        val result = mutableListOf<String>()

        var isInParentheses = false
        var from = 0
        var to = 0

        fun cutNext() {
            val identifier = kotlinToolingProperty.substring(from, to).trim()
            result.add(identifier)
            from = to + 1
        }

        while (to <= kotlinToolingProperty.lastIndex) {
            when (kotlinToolingProperty[to]) {
                ',' -> {
                    if (!isInParentheses) {
                        cutNext()
                    }
                }
                '(' -> isInParentheses = true
                ')' -> isInParentheses = false
            }

            to++
        }

        cutNext()

        return result
    }

    private val kotlinVersionRegex = "([^\\[(\\])]+)(?:\\[([^\\[(\\])]+)])?(?:\\(([^\\[(\\])]+)\\))?".toRegex()

    private fun parseVersion(version: String): SupportedKotlinToolingVersion {
        val trimmedVersion = version.trim()

        val match = kotlinVersionRegex.matchEntire(trimmedVersion) ?: error("Invalid Kotlin version identifier: $trimmedVersion")

        val name = match.groups[1]?.value?.let(::KotlinToolingVersion)
            ?: error("Invalid Kotlin version identifier - missing name: $trimmedVersion")

        val primaryVersion = match.groups[2]?.value?.let(::KotlinToolingVersion) ?: name

        val otherSupportedVersions = match.groups[3]?.value
            ?.split(",")
            ?.map { it.trim() }
            ?.map(::KotlinToolingVersion)
            ?: emptyList()

        return SupportedKotlinToolingVersion(
            name = name,
            primaryVersion = primaryVersion,
            otherSupportedVersions = otherSupportedVersions,
        )
    }
}
