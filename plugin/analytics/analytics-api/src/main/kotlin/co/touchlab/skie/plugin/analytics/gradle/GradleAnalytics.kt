package co.touchlab.skie.plugin.analytics.gradle

import kotlinx.serialization.Serializable

@Serializable
data class GradleAnalytics(
    val rootProjectName: String,
    val projectPath: String,
    val licenseKey: String,
    val skieVersion: String,
    val gradleVersion: String,
    val kotlinVersion: String,
    val stdlibVersion: String,
    val isCI: Boolean,
)
