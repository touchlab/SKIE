package co.touchlab.swiftlink.plugin

import co.touchlab.swiftgen.plugin.internal.SwiftGenIrGenerationExtension
import co.touchlab.swiftlink.plugin.intercept.PhaseInterceptor
import co.touchlab.swiftpack.api.DefaultSkieModule
import co.touchlab.swiftpack.api.SkieContext
import co.touchlab.swiftpack.api.SkieContextKey
import co.touchlab.swiftpack.api.SkieModule
import co.touchlab.swiftpack.api.SwiftPackModuleBuilder
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class SwiftLinkComponentRegistrar: ComponentRegistrar {
    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        configuration.put(
            SkieContextKey,
            DefaultSkieContext(
                DefaultSkieModule(),
            )
        )

        configuration.get(ConfigurationKeys.linkPhaseSwiftPackOutputDir)?.let {
            SwiftPackModuleBuilder.Config.outputDir = it
        }

        IrGenerationExtension.registerExtension(project, SwiftGenIrGenerationExtension(configuration))

        PhaseInterceptor.setupPhaseListeners(configuration)
    }
}

class DefaultSkieContext(
    override val module: SkieModule
): SkieContext
