package co.touchlab.skie.phases

import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.phases.analytics.performance.SkiePerformanceAnalytics
import co.touchlab.skie.phases.swift.SwiftCompilerConfiguration
import co.touchlab.skie.plugin.analytics.AnalyticsCollector
import co.touchlab.skie.swiftmodel.ObjCTypeRenderer
import co.touchlab.skie.util.FrameworkLayout
import co.touchlab.skie.util.Reporter
import co.touchlab.skie.util.directory.SkieBuildDirectory
import co.touchlab.skie.util.directory.SkieDirectories
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.konan.target.AppleConfigurables

interface SkiePhase<C : SkiePhase.Context> {

    context(C)
    fun isActive(): Boolean = true

    context(C)
    fun execute()

    interface Context {

        val context: Context

        val skiePhaseScheduler: SkiePhaseScheduler

        val compilerConfiguration: CompilerConfiguration

        val skieConfiguration: SkieConfiguration

        val swiftCompilerConfiguration: SwiftCompilerConfiguration

        val skieDirectories: SkieDirectories

        val framework: FrameworkLayout

        val analyticsCollector: AnalyticsCollector

        val skiePerformanceAnalyticsProducer: SkiePerformanceAnalytics.Producer

        val reporter: Reporter

        val konanConfig: KonanConfig

        val configurables: AppleConfigurables
            get() = konanConfig.platform.configurables as AppleConfigurables

        val skieBuildDirectory: SkieBuildDirectory
            get() = skieDirectories.buildDirectory

        val descriptorProvider: DescriptorProvider

        val objCTypeRenderer: ObjCTypeRenderer

        fun <T : Any> get(key: CompilerConfigurationKey<T>): T =
            konanConfig.configuration.getNotNull(key)

        fun <T : Any> getOrNull(key: CompilerConfigurationKey<T>): T? =
            konanConfig.configuration.get(key)

        fun <T : Any> getOrCreate(key: CompilerConfigurationKey<T>, defaultValue: () -> T): T =
            getOrNull(key) ?: run {
                val value = defaultValue()

                put(key, value)

                value
            }

        fun <T : Any> put(key: CompilerConfigurationKey<T>, value: T) {
            konanConfig.configuration.put(key, value)
        }
    }
}
