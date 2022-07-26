package co.touchlab.swiftgen.sealed.plugin

import co.touchlab.swiftpack.api.buildSwiftPackModule
import io.outfoxx.swiftpoet.FunctionSpec
import io.outfoxx.swiftpoet.Modifier
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

class SwiftGenSealedComponentRegistrar : ComponentRegistrar {

    override fun registerProjectComponents(project: MockProject, configuration: CompilerConfiguration) {
        IrGenerationExtension.registerExtension(project, TestIrGenerationExtension())
    }
}

internal class TestIrGenerationExtension : IrGenerationExtension {

    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        buildSwiftPackModule("SwiftGen-Sealed") {
            file("Test") {
                addFunction(
                    FunctionSpec.builder("printHelloWorld")
                        .addCode("print(\"Hello world!\")")
                        .addModifiers(Modifier.PUBLIC)
                        .build()
                )
            }
        }
    }
}
