package co.touchlab.skie.plugin

import co.touchlab.skie.api.DefaultSkieContext
import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.configuration.features.SkieFeature
import co.touchlab.skie.kotlin_plugin.BuildConfig
import co.touchlab.skie.plugin.analytics.crash.BugsnagFactory
import co.touchlab.skie.plugin.analytics.producer.AnalyticsCollector
import co.touchlab.skie.plugin.api.SkieContextKey
import co.touchlab.skie.plugin.api.SwiftCompilerConfiguration
import co.touchlab.skie.plugin.api.analytics.SkiePerformanceAnalyticsProducer
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import co.touchlab.skie.plugin.configuration.SkieConfigurationProvider
import co.touchlab.skie.plugin.generator.internal.SkieIrGenerationExtension
import co.touchlab.skie.plugin.intercept.PhaseInterceptor
import co.touchlab.skie.plugin.license.SkieLicenseProvider
import co.touchlab.skie.plugin.reflection.reflectedBy
import co.touchlab.skie.plugin.reflection.reflectors.GroupingMessageCollectorReflector
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity
import org.jetbrains.kotlin.cli.common.messages.CompilerMessageSourceLocation
import org.jetbrains.kotlin.cli.common.messages.GroupingMessageCollector
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class SkieComponentRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = false

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val skieDirectories = configuration.getNotNull(ConfigurationKeys.skieDirectories).also {
            it.resetTemporaryDirectories()
        }

        val license = SkieLicenseProvider.loadLicense(skieDirectories)
        val skieConfiguration = SkieConfigurationProvider.getConfiguration(skieDirectories)

        val swiftCompilerConfiguration = SwiftCompilerConfiguration(
            sourceFilesDirectory = skieDirectories.buildDirectory.swift.directory,
            swiftVersion = configuration.get(ConfigurationKeys.SwiftCompiler.swiftVersion, "5"),
            parallelCompilation = configuration.get(
                ConfigurationKeys.SwiftCompiler.parallelCompilation,
                // WIP Refactor after rebasing licensing
                SkieFeature.ParallelSwiftCompilation in skieConfiguration.enabledFeatures
            ),
            additionalFlags = configuration.getList(ConfigurationKeys.SwiftCompiler.additionalFlags),
        )

        val skieContext = DefaultSkieContext(
            module = DefaultSkieModule(),
            license = license,
            configuration = skieConfiguration,
            swiftCompilerConfiguration = swiftCompilerConfiguration,
            oldSkieBuildDirectory = configuration.getNotNull(ConfigurationKeys.skieBuildDir),
            skieDirectories = skieDirectories,
            frameworkLayout = FrameworkLayout(configuration.getNotNull(KonanConfigKeys.OUTPUT)),
            analyticsCollector = AnalyticsCollector(
                analyticsDirectories = skieDirectories.analyticsDirectories,
                buildId = configuration.getNotNull(ConfigurationKeys.buildId),
                skieVersion = BuildConfig.SKIE_VERSION,
                type = BugsnagFactory.Type.Compiler,
                environment = license.environment,
                configuration = skieConfiguration.analyticsConfiguration,
            ),
            skiePerformanceAnalyticsProducer = SkiePerformanceAnalyticsProducer(),
        )

        configuration.put(SkieContextKey, skieContext)

        registerErrorAnalytics(configuration, skieContext.analyticsCollector)

        IrGenerationExtension.registerExtension(SkieIrGenerationExtension(configuration))

        PhaseInterceptor.setupPhaseListeners(configuration)
    }

    private fun registerErrorAnalytics(configuration: CompilerConfiguration, analyticsCollector: AnalyticsCollector) {
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY) ?: MessageCollector.NONE

        if (messageCollector is GroupingMessageCollector) {
            val reflector = messageCollector.reflectedBy<GroupingMessageCollectorReflector>()

            val delegate = reflector.delegate

            reflector.delegate = MessageCollectorWithSkieAnalytics(analyticsCollector, delegate)
        } else {
            val messageCollectorWithSkieAnalytics = MessageCollectorWithSkieAnalytics(analyticsCollector, messageCollector)

            configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, messageCollectorWithSkieAnalytics)
        }
    }

    private class MessageCollectorWithSkieAnalytics(
        private val analyticsCollector: AnalyticsCollector,
        private val delegate: MessageCollector,
    ) : MessageCollector {

        override fun clear() {
            delegate.clear()
        }

        override fun hasErrors(): Boolean =
            delegate.hasErrors()

        override fun report(severity: CompilerMessageSeverity, message: String, location: CompilerMessageSourceLocation?) {
            if (severity.isError) {
                analyticsCollector.logException(message)
            }

            delegate.report(severity, message, location)
        }
    }
}
