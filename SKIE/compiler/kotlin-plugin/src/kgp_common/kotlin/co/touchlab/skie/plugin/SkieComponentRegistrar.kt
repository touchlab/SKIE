package co.touchlab.skie.plugin

import co.touchlab.skie.api.DefaultSkieContext
import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.kotlin_plugin.BuildConfig
import co.touchlab.skie.plugin.analytics.producer.AnalyticsCollector
import co.touchlab.skie.plugin.api.SkieContextKey
import co.touchlab.skie.plugin.api.SwiftCompilerConfiguration
import co.touchlab.skie.plugin.api.analytics.SkiePerformanceAnalyticsProducer
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import co.touchlab.skie.plugin.generator.internal.SkieIrGenerationExtension
import co.touchlab.skie.plugin.intercept.PhaseInterceptorRegistrar
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class SkieComponentRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = false

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val skieDirectories = configuration.getNotNull(ConfigurationKeys.skieDirectories).also {
            it.resetTemporaryDirectories()
        }

        val serializedUserConfiguration = skieDirectories.buildDirectory.skieConfiguration.readText()
        val skieConfiguration = Configuration.deserialize(serializedUserConfiguration)

        val swiftCompilerConfiguration = SwiftCompilerConfiguration(
            sourceFilesDirectory = skieDirectories.buildDirectory.swift.directory,
            swiftVersion = configuration.get(ConfigurationKeys.SwiftCompiler.swiftVersion, "5"),
            additionalFlags = configuration.getList(ConfigurationKeys.SwiftCompiler.additionalFlags),
        )

        val skieContext = DefaultSkieContext(
            module = DefaultSkieModule(),
            configuration = skieConfiguration,
            swiftCompilerConfiguration = swiftCompilerConfiguration,
            skieDirectories = skieDirectories,
            frameworkLayout = FrameworkLayout(configuration.getNotNull(KonanConfigKeys.OUTPUT)),
            analyticsCollector = AnalyticsCollector(
                analyticsDirectories = skieDirectories.analyticsDirectories,
                buildId = configuration.getNotNull(ConfigurationKeys.buildId),
                skieVersion = BuildConfig.SKIE_VERSION,
                configuration = skieConfiguration.analyticsConfiguration,
            ),
            skiePerformanceAnalyticsProducer = SkiePerformanceAnalyticsProducer(),
        )

        configuration.put(SkieContextKey, skieContext)

        IrGenerationExtension.registerExtension(SkieIrGenerationExtension(configuration))

        PhaseInterceptorRegistrar.setupPhaseInterceptors(configuration)
    }
}
