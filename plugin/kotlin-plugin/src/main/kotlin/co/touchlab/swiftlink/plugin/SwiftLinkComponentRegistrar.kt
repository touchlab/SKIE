package co.touchlab.swiftlink.plugin

import co.touchlab.swiftgen.plugin.internal.SwiftGenIrGenerationExtension
import co.touchlab.swiftlink.plugin.intercept.PhaseInterceptor
import co.touchlab.swiftpack.api.impl.DefaultSkieModule
import co.touchlab.swiftpack.api.SkieContextKey
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

        IrGenerationExtension.registerExtension(project, SwiftGenIrGenerationExtension(configuration))

        PhaseInterceptor.setupPhaseListeners(configuration)
    }
}
