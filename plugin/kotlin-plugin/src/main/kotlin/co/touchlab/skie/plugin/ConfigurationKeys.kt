package co.touchlab.skie.plugin

import org.jetbrains.kotlin.config.CompilerConfigurationKey
import java.io.File

object ConfigurationKeys {

    val swiftSourceFiles = CompilerConfigurationKey<List<File>>("Swift source files")
    val generatedSwiftDir = CompilerConfigurationKey<File>("generated Swift directory")
    val disableWildcardExport = CompilerConfigurationKey<Boolean>("disable wildcard export")
    val swiftLinkLogFile = CompilerConfigurationKey<File>("Swift compiler log file")
}
