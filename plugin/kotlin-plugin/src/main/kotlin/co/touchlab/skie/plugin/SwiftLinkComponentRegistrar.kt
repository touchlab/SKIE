package co.touchlab.skie.plugin

import co.touchlab.skie.api.impl.DefaultSkieContext
import co.touchlab.skie.api.impl.DefaultSkieModule
import co.touchlab.skie.plugin.api.SkieContextKey
import co.touchlab.skie.plugin.api.util.FrameworkLayout
import co.touchlab.skie.plugin.generator.internal.SwiftGenIrGenerationExtension
import co.touchlab.skie.plugin.intercept.PhaseInterceptor
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.konan.KonanConfigKeys
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class SwiftLinkComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        configuration.put(
            SkieContextKey,
            DefaultSkieContext(
                module = DefaultSkieModule(),
                swiftSourceFiles = configuration.getList(ConfigurationKeys.swiftSourceFiles),
                expandedSwiftDir = configuration.getNotNull(ConfigurationKeys.generatedSwiftDir),
                frameworkLayout = FrameworkLayout(configuration.getNotNull(KonanConfigKeys.OUTPUT)),
                disableWildcardExport = configuration.getBoolean(ConfigurationKeys.disableWildcardExport),
            )
        )

        IrGenerationExtension.registerExtension(project, SwiftGenIrGenerationExtension(configuration))

        PhaseInterceptor.setupPhaseListeners(configuration)
    }
}
