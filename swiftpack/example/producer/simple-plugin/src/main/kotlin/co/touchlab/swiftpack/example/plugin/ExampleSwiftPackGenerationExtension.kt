package co.touchlab.swiftpack.example.plugin

import co.touchlab.swiftpack.api.buildSwiftPackModule
import co.touchlab.swiftpack.plugin.SwiftPackGenerationExtension
import com.google.auto.service.AutoService
import io.outfoxx.swiftpoet.Modifier
import io.outfoxx.swiftpoet.PropertySpec
import io.outfoxx.swiftpoet.STRING
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

@AutoService(SwiftPackGenerationExtension::class)
class ExampleSwiftPackGenerationExtension: SwiftPackGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        buildSwiftPackModule("simple-example") {
            file("HelloWorld") {
                addProperty(
                    PropertySpec.builder("helloWorld", STRING, Modifier.PUBLIC)
                        .initializer("%S", "Hello World")
                        .build()
                )
            }
        }
    }
}
