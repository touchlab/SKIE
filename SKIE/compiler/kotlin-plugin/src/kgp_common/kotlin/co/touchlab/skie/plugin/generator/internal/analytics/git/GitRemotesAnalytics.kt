package co.touchlab.skie.plugin.generator.internal.analytics.git

import kotlinx.serialization.Serializable

@Serializable
data class GitRemotesAnalytics(
    val gitRemotes: List<String>,
)
