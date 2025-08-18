package co.touchlab.skie.gradle.version

import org.gradle.api.Project

data class MinGradleVersion(
    val gradle: String,
    val embeddedKotlin: String,
    val embeddedGroovy: String,
)

fun Project.minGradleVersion(): MinGradleVersion =
    MinGradleVersion(
        gradle = property("versionSupport.gradle.minVersion")?.toString() ?: error("Missing property 'versionSupport.gradle.minVersion'"),
        embeddedKotlin = property("versionSupport.gradle.embeddedKotlin")?.toString() ?: error("Missing property 'versionSupport.gradle.embeddedKotlin'"),
        embeddedGroovy = property("versionSupport.gradle.embeddedGroovy")?.toString() ?: error("Missing property 'versionSupport.gradle.embeddedGroovy'"),
    )


