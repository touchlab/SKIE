package co.touchlab.skie.phases

import co.touchlab.skie.configuration.ConfigurationProvider
import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.phases.analytics.performance.SkiePerformanceAnalytics
import co.touchlab.skie.phases.swift.SwiftCompilerConfiguration
import co.touchlab.skie.plugin.analytics.AnalyticsCollector
import co.touchlab.skie.util.FrameworkLayout
import co.touchlab.skie.util.Reporter
import co.touchlab.skie.util.directory.SkieBuildDirectory
import co.touchlab.skie.util.directory.SkieDirectories
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

object InitPhase {

    interface Context {

        val context: Context

        val skiePhaseScheduler: SkiePhaseScheduler

        val compilerConfiguration: CompilerConfiguration

        val skieConfiguration: SkieConfiguration

        val configurationProvider: ConfigurationProvider

        val swiftCompilerConfiguration: SwiftCompilerConfiguration

        val skieDirectories: SkieDirectories

        val framework: FrameworkLayout

        val analyticsCollector: AnalyticsCollector

        val skiePerformanceAnalyticsProducer: SkiePerformanceAnalytics.Producer

        val reporter: Reporter

        val skieBuildDirectory: SkieBuildDirectory
            get() = skieDirectories.buildDirectory

        fun <T : Any> get(key: CompilerConfigurationKey<T>): T =
            compilerConfiguration.getNotNull(key)

        fun <T : Any> getOrNull(key: CompilerConfigurationKey<T>): T? =
            compilerConfiguration.get(key)

        fun <T : Any> getOrCreate(key: CompilerConfigurationKey<T>, defaultValue: () -> T): T =
            getOrNull(key) ?: run {
                val value = defaultValue()

                put(key, value)

                value
            }

        fun <T : Any> put(key: CompilerConfigurationKey<T>, value: T) {
            compilerConfiguration.put(key, value)
        }
    }
}
