package co.touchlab.skie.plugin

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion

object KotlinVersionChecker {

    private val compatibleVersions = listOf(
        "1.8.20",
        "1.8.21",
    )

    fun ensureProjectUsesCompatibleKotlinVersion(project: Project) {
        val kotlinCompilerVersion = project.getKotlinPluginVersion()

        val isCompatibleWithSkie = compatibleVersions.any { it == kotlinCompilerVersion }

        check(isCompatibleWithSkie) {
            "Kotlin version $kotlinCompilerVersion is not compatible with SKIE. Compatible versions: $compatibleVersions"
        }
    }
}
