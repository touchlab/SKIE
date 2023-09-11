package co.touchlab.skie.compilerinject.plugin

import co.touchlab.skie.phases.analytics.performance.SkiePerformanceAnalytics
import co.touchlab.skie.plugin.analytics.AnalyticsCollector
import co.touchlab.skie.configuration.SkieConfiguration
import co.touchlab.skie.util.FrameworkLayout
import co.touchlab.skie.util.Reporter
import co.touchlab.skie.compilerinject.interceptor.PhaseInterceptorRegistrar
import co.touchlab.skie.compilerinject.reflection.ConfigurationKeys
import co.touchlab.skie.compilerinject.reflection.SkieContextKey
import co.touchlab.skie.entrypoint.SkieIrGenerationExtension
import co.touchlab.skie.phases.DefaultSkieContext
import co.touchlab.skie.phases.DefaultSkieModule
import co.touchlab.skie.phases.swift.SwiftCompilerConfiguration
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class SkieComponentRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = false

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val skieDirectories = configuration.getNotNull(ConfigurationKeys.skieDirectories)

        val serializedUserConfiguration = skieDirectories.buildDirectory.skieConfiguration.readText()
        val skieConfiguration = SkieConfiguration.deserialize(serializedUserConfiguration)

        val swiftCompilerConfiguration = SwiftCompilerConfiguration(
            sourceFilesDirectory = skieDirectories.buildDirectory.swift.directory,
            swiftVersion = configuration.get(ConfigurationKeys.SwiftCompiler.swiftVersion, "5"),
            additionalFlags = configuration.getList(ConfigurationKeys.SwiftCompiler.additionalFlags),
        )

        val skieContext = DefaultSkieContext(
            module = DefaultSkieModule(),
            skieConfiguration = skieConfiguration,
            swiftCompilerConfiguration = swiftCompilerConfiguration,
            skieDirectories = skieDirectories,
            frameworkLayout = FrameworkLayout(configuration.getNotNull(KonanConfigKeys.OUTPUT)),
            analyticsCollector = AnalyticsCollector(
                skieBuildDirectory = skieDirectories.buildDirectory,
                skieConfiguration = skieConfiguration,
            ),
            skiePerformanceAnalyticsProducer = SkiePerformanceAnalytics.Producer(skieConfiguration),
            reporter = Reporter(configuration),
        )

        configuration.put(SkieContextKey, skieContext)

        IrGenerationExtension.registerExtension(SkieIrGenerationExtension(configuration))

        PhaseInterceptorRegistrar.setupPhaseInterceptors(configuration)
    }
}
