package co.touchlab.skie.buildsetup.util.version

import org.gradle.api.Project

object SupportedKotlinVersionProvider {

    fun getPrimaryKotlinVersion(project: Project): SupportedKotlinVersion {
        val enabledKotlinVersions = getEnabledKotlinVersions(project)

        val activeSupportedVersion = enabledKotlinVersions.maxByOrNull { it.name }
            ?: error("Primary Kotlin version cannot be determined as no supported versions are enabled.")

        return activeSupportedVersion
    }

    fun getEnabledKotlinVersions(project: Project): List<SupportedKotlinVersion> {
        val enabledVersions = project.findProperty("versionSupport.kotlin.enabledVersions")
            ?.toString()
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() }
            ?: emptyList()

        val supportedKotlinVersions = getSupportedKotlinVersions(project)

        return if (enabledVersions.isNotEmpty()) {
            supportedKotlinVersions.filter { it.name.toString() in enabledVersions }
        } else {
            supportedKotlinVersions
        }
    }

    fun getMinimumSupportedKotlinVersion(project: Project): KotlinToolingVersion =
        getSupportedKotlinVersions(project).flatMap { it.supportedVersions }.min()

    /**
     * Supported format:
     * ```
     * versionSupport.kotlin=1.8.0, 1.8.20(1.8.21, 1.8.22), 1.9.0[1.9.0-RC](1.9.10), 1.9.20[1.9.20-RC]
     * ```
     *
     * The first version is used as the name of the component and as the compiler version.
     * The [] parentheses can be used to override the compiler version, which can be useful for new version development
     * against and RC version when the full version is not available yet.
     * Otherwise, we would have to rename everything once the full version is released.
     *
     * The () parentheses can be used to specify other supported versions (the compiler version is supported by default).
     */
    fun getSupportedKotlinVersions(project: Project): List<SupportedKotlinVersion> {
        val supportedKotlinVersions = project.property("versionSupport.kotlin").toString()

        val versions = splitVersions(supportedKotlinVersions)

        return versions.map { parseVersion(it) }
    }

    private fun splitVersions(supportedKotlinVersions: String): List<String> {
        val result = mutableListOf<String>()

        var isInParentheses = false
        var from = 0
        var to = 0

        fun cutNext() {
            val identifier = supportedKotlinVersions.substring(from, to).trim()
            result.add(identifier)
            from = to + 1
        }

        while (to <= supportedKotlinVersions.lastIndex) {
            when (supportedKotlinVersions[to]) {
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

    private fun parseVersion(version: String): SupportedKotlinVersion {
        val trimmedVersion = version.trim()

        val match = kotlinVersionRegex.matchEntire(trimmedVersion) ?: error("Invalid Kotlin version identifier: $trimmedVersion")

        val name = match.groups[1]?.value?.let(::KotlinToolingVersion)
            ?: error("Invalid Kotlin version identifier - missing name: $trimmedVersion")

        val compilerVersion = match.groups[2]?.value?.let(::KotlinToolingVersion) ?: name

        val otherSupportedVersions = match.groups[3]?.value
            ?.split(",")
            ?.map { it.trim() }
            ?.map(::KotlinToolingVersion)
            ?: emptyList()

        return SupportedKotlinVersion(
            name = name,
            compilerVersion = compilerVersion,
            otherSupportedVersions = otherSupportedVersions,
        )
    }
}
