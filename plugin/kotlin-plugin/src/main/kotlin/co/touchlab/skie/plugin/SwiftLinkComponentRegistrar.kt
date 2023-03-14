package co.touchlab.skie.plugin

import co.touchlab.skie.api.DefaultSkieContext
import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.api.SkieContextKey
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import co.touchlab.skie.plugin.generator.internal.SkieIrGenerationExtension
import co.touchlab.skie.plugin.intercept.PhaseInterceptor
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class SkieComponentRegistrar : CompilerPluginRegistrar() {

    override val supportsK2: Boolean = false

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        configuration.put(
            SkieContextKey,
            DefaultSkieContext(
                module = DefaultSkieModule(),
                configuration = configuration.get(ConfigurationKeys.skieConfiguration, Configuration {}),
                swiftSourceFiles = configuration.getList(ConfigurationKeys.swiftSourceFiles),
                expandedSwiftDir = configuration.getNotNull(ConfigurationKeys.generatedSwiftDir),
                debugInfoDirectory = configuration.getNotNull(ConfigurationKeys.Debug.infoDirectory),
                frameworkLayout = FrameworkLayout(configuration.getNotNull(KonanConfigKeys.OUTPUT)),
                disableWildcardExport = configuration.getBoolean(ConfigurationKeys.disableWildcardExport),
                dumpSwiftApiPoints = configuration.get(ConfigurationKeys.Debug.dumpSwiftApiPoints) ?: emptySet(),
            )
        )

        IrGenerationExtension.registerExtension(SkieIrGenerationExtension(configuration))

        PhaseInterceptor.setupPhaseListeners(configuration)
    }
}
