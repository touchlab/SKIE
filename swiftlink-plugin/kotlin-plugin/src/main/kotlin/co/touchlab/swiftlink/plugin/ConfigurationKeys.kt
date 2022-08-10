package co.touchlab.swiftlink.plugin

import co.touchlab.swiftpack.spi.NamespacedSwiftPackModule
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import java.io.File

object ConfigurationKeys {
    val isEnabled = CompilerConfigurationKey<Boolean>("SwiftKt enabled")
    val swiftPackModules = CompilerConfigurationKey<List<NamespacedSwiftPackModule.Reference>>("SwiftPack modules")
    val swiftSourceFiles = CompilerConfigurationKey<List<File>>("Swift source files")
    val expandedSwiftDir = CompilerConfigurationKey<File>("expanded Swift directory")
}
