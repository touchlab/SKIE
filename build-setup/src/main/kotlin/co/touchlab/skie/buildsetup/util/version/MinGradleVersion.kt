package co.touchlab.skie.buildsetup.util.version

import org.gradle.api.Project

data class MinGradleVersion(
    val gradle: String,
    val embeddedKotlin: String,
    val embeddedGroovy: String,
)

fun Project.minGradleVersion(): MinGradleVersion =
    MinGradleVersion(
        gradle = property("versionSupport.gradle.minVersion").toString(),
        embeddedKotlin = property("versionSupport.gradle.embeddedKotlin").toString(),
        embeddedGroovy = property("versionSupport.gradle.embeddedGroovy").toString(),
    )
