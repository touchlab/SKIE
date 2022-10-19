package co.touchlab.swiftgen.plugin

import co.touchlab.swiftgen.plugin.internal.SwiftGenIrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class SwiftGenComponentRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        val irGenerationExtension = SwiftGenIrGenerationExtension(configuration)

        IrGenerationExtension.registerExtension(project, irGenerationExtension)
    }
}
