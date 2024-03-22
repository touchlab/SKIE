@file:Suppress("invisible_reference", "invisible_member")

package co.touchlab.skie.context

import co.touchlab.skie.compilerinject.compilerplugin.SkieConfigurationKeys
import co.touchlab.skie.configuration.ConfigurationProvider
import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.phases.InitPhase
import co.touchlab.skie.phases.SkiePhaseScheduler
import co.touchlab.skie.phases.analytics.performance.SkiePerformanceAnalytics
import co.touchlab.skie.phases.swift.SwiftCompilerConfiguration
import co.touchlab.skie.plugin.analytics.AnalyticsCollector
import co.touchlab.skie.util.FrameworkLayout
import co.touchlab.skie.util.Reporter
import co.touchlab.skie.util.directory.SkieDirectories
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.config.CompilerConfiguration

class InitPhaseContext(
    override val compilerConfiguration: CompilerConfiguration,
) : InitPhase.Context {

    override val context: InitPhase.Context
        get() = this

    override val skiePhaseScheduler: SkiePhaseScheduler = SkiePhaseScheduler()

    override val skieDirectories: SkieDirectories = compilerConfiguration.getNotNull(SkieConfigurationKeys.SkieDirectories)

    override val skieConfiguration: SkieConfiguration = run {
        val serializedUserConfiguration = skieDirectories.buildDirectory.skieConfiguration.readText()

        SkieConfiguration.deserialize(serializedUserConfiguration)
    }

    override val configurationProvider: ConfigurationProvider = ConfigurationProvider(skieConfiguration)

    override val swiftCompilerConfiguration: SwiftCompilerConfiguration = SwiftCompilerConfiguration(
        sourceFilesDirectory = skieDirectories.buildDirectory.swift.directory,
        swiftVersion = compilerConfiguration.get(SkieConfigurationKeys.SwiftCompiler.swiftVersion, "5"),
        additionalFlags = compilerConfiguration.getList(SkieConfigurationKeys.SwiftCompiler.additionalFlags),
    )

    override val analyticsCollector: AnalyticsCollector = AnalyticsCollector(
        skieBuildDirectory = skieDirectories.buildDirectory,
        skieConfiguration = skieConfiguration,
    )

    override val skiePerformanceAnalyticsProducer: SkiePerformanceAnalytics.Producer = SkiePerformanceAnalytics.Producer(skieConfiguration)

    override val reporter: Reporter = Reporter(compilerConfiguration)

    override val framework: FrameworkLayout = run {
        val frameworkPath = compilerConfiguration.getNotNull(KonanConfigKeys.OUTPUT)

        FrameworkLayout(frameworkPath)
    }
}
