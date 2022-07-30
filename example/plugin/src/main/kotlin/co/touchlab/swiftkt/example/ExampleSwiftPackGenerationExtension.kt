package co.touchlab.swiftkt.example

import co.touchlab.swiftpack.api.buildSwiftPackModule
import co.touchlab.swiftpack.api.kotlin
import co.touchlab.swiftpack.plugin.SwiftPackGenerationExtension
import com.google.auto.service.AutoService
import io.outfoxx.swiftpoet.DeclaredTypeName
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

@AutoService(SwiftPackGenerationExtension::class)
class ExampleSwiftPackGenerationExtension: SwiftPackGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        buildSwiftPackModule("swiftkt-example") {
            kobjcTransforms {
                hide("co.touchlab.swiftkt.ToBeHidden")
                rename("co.touchlab.swiftkt.ToBeRenamed", "Renamed")
            }
        }
    }
}
