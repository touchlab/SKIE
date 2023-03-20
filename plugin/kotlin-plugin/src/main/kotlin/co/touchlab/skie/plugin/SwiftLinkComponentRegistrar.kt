package co.touchlab.skie.plugin

import co.touchlab.skie.api.DefaultSkieContext
import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.kotlin_plugin.BuildConfig
import co.touchlab.skie.plugin.analytics.crash.BugsnagFactory
import co.touchlab.skie.plugin.analytics.producer.AnalyticsCollector
import co.touchlab.skie.plugin.api.SkieComponentContainerKey
import co.touchlab.skie.plugin.api.SkieContextKey
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import co.touchlab.skie.plugin.generator.internal.SkieIrGenerationExtension
import co.touchlab.skie.plugin.generator.internal.registerGeneratorComponents
import co.touchlab.skie.plugin.intercept.PhaseInterceptorRegistrar
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
import org.jetbrains.kotlin.container.composeContainer
import org.jetbrains.kotlin.container.useImpl
import org.jetbrains.kotlin.container.useInstance

class SkieComponentRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = false

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        val license = SkieLicenseProvider.getLicense(configuration.getNotNull(ConfigurationKeys.jwtWithLicense))
        val skieConfiguration = configuration.get(ConfigurationKeys.skieConfiguration, Configuration {})

        val skieContext = DefaultSkieContext(
            // TODO: Remove SkieModule from SkieContext
            module = DefaultSkieModule(),
            configuration = skieConfiguration,
            swiftSourceFiles = configuration.getList(ConfigurationKeys.swiftSourceFiles),
            expandedSwiftDir = configuration.getNotNull(ConfigurationKeys.generatedSwiftDir),
            debugInfoDirectory = configuration.getNotNull(ConfigurationKeys.Debug.infoDirectory),
            frameworkLayout = FrameworkLayout(configuration.getNotNull(KonanConfigKeys.OUTPUT)),
            disableWildcardExport = configuration.getBoolean(ConfigurationKeys.disableWildcardExport),
            dumpSwiftApiPoints = configuration.get(ConfigurationKeys.Debug.dumpSwiftApiPoints) ?: emptySet(),
            analyticsCollector = AnalyticsCollector(
                analyticsDirectory = configuration.getNotNull(ConfigurationKeys.analyticsDir).toPath(),
                buildId = configuration.getNotNull(ConfigurationKeys.buildId),
                skieVersion = BuildConfig.SKIE_VERSION,
                type = BugsnagFactory.Type.Compiler,
                license = license,
                configuration = skieConfiguration.analyticsConfiguration,
            ),
        )

        configuration.put(SkieContextKey, skieContext)

        registerErrorAnalytics(configuration, skieContext.analyticsCollector)

        val skieContainer = composeContainer("Skie") {
            useInstance(skieContext)
            useImpl<DefaultSkieModule>()

            registerGeneratorComponents()
        }
        configuration.put(SkieComponentContainerKey, skieContainer)

        // TODO: Should this be accessible only from container?
        configuration.put(SkieContextKey, skieContext)

        IrGenerationExtension.registerExtension(SkieIrGenerationExtension(configuration))
        PhaseInterceptorRegistrar.setupPhaseListeners(configuration)
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

sealed interface SkiePhase {
    val name: String


}
