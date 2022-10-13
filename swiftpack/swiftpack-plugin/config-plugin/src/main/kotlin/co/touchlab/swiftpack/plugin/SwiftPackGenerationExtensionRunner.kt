package co.touchlab.swiftpack.plugin

import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.cli.jvm.plugins.ServiceLoaderLite
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import java.net.URLClassLoader
import java.util.*
import kotlin.reflect.KClass

class SwiftPackGenerationExtensionRunner: IrGenerationExtension {
    override fun generate(moduleFragment: IrModuleFragment, pluginContext: IrPluginContext) {
        ServiceLoaderLite.loadImplementations(SwiftPackGenerationExtension::class.java, this::class.java.classLoader as URLClassLoader)
            .forEach {
                it.generate(moduleFragment, pluginContext)
            }
    }
}