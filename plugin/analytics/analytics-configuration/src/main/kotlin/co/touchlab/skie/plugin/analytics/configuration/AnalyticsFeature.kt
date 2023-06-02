package co.touchlab.skie.plugin.analytics.configuration

import kotlinx.serialization.Serializable

@Serializable
sealed interface AnalyticsFeature {

    val isEnabled: Boolean

    @Serializable
    data class Air(override val isEnabled: Boolean, val stripIdentifiers: Boolean) : AnalyticsFeature

    @Serializable
    data class Compiler(override val isEnabled: Boolean, val stripIdentifiers: Boolean) : AnalyticsFeature

    @Serializable
    data class Gradle(override val isEnabled: Boolean, val stripIdentifiers: Boolean) : AnalyticsFeature

    @Serializable
    data class Hardware(override val isEnabled: Boolean) : AnalyticsFeature

    @Serializable
    data class GradlePerformance(override val isEnabled: Boolean) : AnalyticsFeature

    @Serializable
    data class SkiePerformance(override val isEnabled: Boolean) : AnalyticsFeature

    @Serializable
    data class SkieConfiguration(override val isEnabled: Boolean, val stripIdentifiers: Boolean) : AnalyticsFeature

    @Serializable
    data class Sysctl(override val isEnabled: Boolean) : AnalyticsFeature

    @Serializable
    data class CrashReporting(override val isEnabled: Boolean) : AnalyticsFeature
}
