package co.touchlab.skie.configuration.gradle

import co.touchlab.skie.plugin.analytics.configuration.AnalyticsConfiguration
import co.touchlab.skie.plugin.analytics.configuration.AnalyticsFeature

enum class AnalyticsTier {
    /**
     * Same as .NoIdentifiers but also logs identifiers (for example, function names).
     */
    Full {

        override fun buildAnalyticsConfiguration(): AnalyticsConfiguration =
            AnalyticsConfiguration(
                AnalyticsFeature.CrashReporting(isEnabled = true),
                AnalyticsFeature.Gradle(isEnabled = true, stripIdentifiers = false),
                AnalyticsFeature.SkieConfiguration(isEnabled = true, stripIdentifiers = false),
                AnalyticsFeature.Compiler(isEnabled = true, stripIdentifiers = false),
                AnalyticsFeature.Hardware(isEnabled = true),
                AnalyticsFeature.SkiePerformance(isEnabled = true),
                AnalyticsFeature.GradlePerformance(isEnabled = true),
                AnalyticsFeature.Sysctl(isEnabled = true),
                AnalyticsFeature.Air(isEnabled = true, stripIdentifiers = false),
            )
    },
    /**
     * Same as .NoDescriptors + code declarations (information about classes, functions, etc.).
     * The analytics logs only the declarations not expressions (the content of functions).
     * Also, no identifiers unique to the project are sent.
     */
    NoIdentifiers {

        override fun buildAnalyticsConfiguration(): AnalyticsConfiguration =
            AnalyticsConfiguration(
                AnalyticsFeature.CrashReporting(isEnabled = true),
                AnalyticsFeature.Gradle(isEnabled = true, stripIdentifiers = true),
                AnalyticsFeature.SkieConfiguration(isEnabled = true, stripIdentifiers = true),
                AnalyticsFeature.Compiler(isEnabled = true, stripIdentifiers = true),
                AnalyticsFeature.Hardware(isEnabled = true),
                AnalyticsFeature.SkiePerformance(isEnabled = true),
                AnalyticsFeature.GradlePerformance(isEnabled = true),
                AnalyticsFeature.Sysctl(isEnabled = true),
                AnalyticsFeature.Air(isEnabled = true, stripIdentifiers = true),
            )
    },
    /**
     * Same as .Minimal + HW information.
     */
    NoDescriptors {

        override fun buildAnalyticsConfiguration(): AnalyticsConfiguration =
            AnalyticsConfiguration(
                AnalyticsFeature.CrashReporting(isEnabled = true),
                AnalyticsFeature.Gradle(isEnabled = true, stripIdentifiers = true),
                AnalyticsFeature.SkieConfiguration(isEnabled = true, stripIdentifiers = true),
                AnalyticsFeature.Compiler(isEnabled = true, stripIdentifiers = true),
                AnalyticsFeature.Hardware(isEnabled = true),
                AnalyticsFeature.SkiePerformance(isEnabled = true),
                AnalyticsFeature.GradlePerformance(isEnabled = true),
                AnalyticsFeature.Sysctl(isEnabled = true),
            )
    },

    /**
     * Logs only basic debugging and license information.
     */
    Minimal {

        override fun buildAnalyticsConfiguration(): AnalyticsConfiguration =
            AnalyticsConfiguration(
                AnalyticsFeature.CrashReporting(isEnabled = true),
                AnalyticsFeature.Gradle(isEnabled = true, stripIdentifiers = true),
                AnalyticsFeature.SkiePerformance(isEnabled = true),
                AnalyticsFeature.GradlePerformance(isEnabled = true),
                AnalyticsFeature.SkieConfiguration(isEnabled = true, stripIdentifiers = true),
                AnalyticsFeature.Compiler(isEnabled = true, stripIdentifiers = true),
            )
    };

    abstract fun buildAnalyticsConfiguration(): AnalyticsConfiguration
}
