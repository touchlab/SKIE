package co.touchlab.skie.plugin

import co.touchlab.skie.api.DefaultSkieContext
import co.touchlab.skie.api.DefaultSkieModule
import co.touchlab.skie.plugin.api.SkieContextKey
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import co.touchlab.skie.plugin.generator.internal.SwiftGenIrGenerationExtension
import co.touchlab.skie.plugin.intercept.PhaseInterceptor
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class SkieComponentRegistrar: CompilerPluginRegistrar() {
    override val supportsK2: Boolean = false

    override fun ExtensionStorage.registerExtensions(configuration: CompilerConfiguration) {
        configuration.put(
            SkieContextKey,
            DefaultSkieContext(
                module = DefaultSkieModule(),
                swiftSourceFiles = configuration.getList(ConfigurationKeys.swiftSourceFiles),
                expandedSwiftDir = configuration.getNotNull(ConfigurationKeys.generatedSwiftDir),
                swiftLinkLogFile = configuration.getNotNull(ConfigurationKeys.swiftLinkLogFile),
                frameworkLayout = FrameworkLayout(configuration.getNotNull(KonanConfigKeys.OUTPUT)),
                disableWildcardExport = configuration.getBoolean(ConfigurationKeys.disableWildcardExport),
            )
        )

        IrGenerationExtension.registerExtension(SwiftGenIrGenerationExtension(configuration))

        PhaseInterceptor.setupPhaseListeners(configuration)
    }
}
