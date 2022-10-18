package co.touchlab.swiftlink.plugin

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftpack.spec.module.SwiftPackModule
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import java.io.File

object ConfigurationKeys {
    val swiftPackModules = CompilerConfigurationKey<List<SwiftPackModule.Reference>>("SwiftPack modules")
    val swiftSourceFiles = CompilerConfigurationKey<List<File>>("Swift source files")
    val expandedSwiftDir = CompilerConfigurationKey<File>("expanded Swift directory")
    val linkPhaseSwiftPackOutputDir = CompilerConfigurationKey<File>("link phase SwiftPack output directory")
    val disableWildcardExport = CompilerConfigurationKey<Boolean>("disable wildcard export")
}
