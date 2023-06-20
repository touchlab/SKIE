package co.touchlab.skie.gradle.version

import co.touchlab.skie.gradle.util.stringListProperty
import co.touchlab.skie.gradle.version.target.DimensionWithAliases
import co.touchlab.skie.gradle.version.target.Target
import org.gradle.api.Project

fun Project.gradleApiVersionDimension(): Target.Dimension<GradleApiVersionComponent> {
    val requestedIdentifiers = project.stringListProperty("versionSupport.gradleApi")
    return ToolingVersions.Gradle.dimensionFrom(requestedIdentifiers)
}

fun Project.kotlinToolingVersionDimension(): Target.Dimension<KotlinToolingVersionComponent> {
    val requestedIdentifiers = project.stringListProperty("versionSupport.kotlinTooling")
    return ToolingVersions.Kotlin.dimensionFrom(requestedIdentifiers)
}

fun Project.darwinPlatformDimension(): Target.Dimension<DarwinPlatformComponent> {
    return DimensionWithAliases(
        name = null,
        commonName = "darwin",
        components = DarwinPlatformComponent.values().toSet(),
        aliases = mapOf(
            "ios" to setOf(
                DarwinPlatformComponent.iosArm32,
                DarwinPlatformComponent.iosArm64,
                DarwinPlatformComponent.iosX64,
                DarwinPlatformComponent.iosSimulatorArm64,
            ),
            "watchos" to setOf(
                DarwinPlatformComponent.watchosArm32,
                DarwinPlatformComponent.watchosArm64,
                DarwinPlatformComponent.watchosX86,
                DarwinPlatformComponent.watchosX64,
                DarwinPlatformComponent.watchosSimulatorArm64,
            ),
            "tvos" to setOf(
                DarwinPlatformComponent.tvosArm64,
                DarwinPlatformComponent.tvosX64,
                DarwinPlatformComponent.tvosSimulatorArm64,
            ),
            "macos" to setOf(
                DarwinPlatformComponent.macosX64,
                DarwinPlatformComponent.macosArm64,
            ),
        ),
    )
}
