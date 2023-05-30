package co.touchlab.skie.plugin.analytics.opensource

import kotlinx.serialization.Serializable

@Serializable
data class OpenSourceAnalytics(
    val rootProjectName: String,
    val projectFullName: String,
    val gitRemotes: List<String>,
)
