package co.touchlab.skie.plugin.analytics.gradle

import kotlinx.serialization.Serializable

@Serializable
data class GradleAnalytics(
    val rootProjectName: String,
    val rootProjectNameHash: String,
    val projectPath: String,
    val projectFullName: String,
    val projectFullNameHash: String,
    val rootProjectDiskLocationHash: String,
    val organizationKey: String,
    val licenseKey: String,
    val skieVersion: String,
    val gradleVersion: String,
    val kotlinVersion: String,
    val stdlibVersion: String,
    val timestampInMs: Long,
    val isCI: Boolean,
)
// WIP add build id to analytics
