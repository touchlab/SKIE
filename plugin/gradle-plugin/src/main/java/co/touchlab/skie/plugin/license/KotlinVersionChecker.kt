package co.touchlab.skie.plugin.license

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

internal object KotlinVersionChecker {

    private val compatibleVersions = listOf(
        "1.8.0",
        "1.8.10",
    )

    // WIP based on license
    fun ensureProjectUsesCompatibleKotlinVersion(project: Project) {
        val kotlinCompilerVersion = project.getKotlinPluginVersion()

        val isCompatibleWithSkie = compatibleVersions.any { it == kotlinCompilerVersion }

        check(isCompatibleWithSkie) {
            "Kotlin version $kotlinCompilerVersion is not compatible with SKIE. Compatible versions: $compatibleVersions"
        }
    }
}
