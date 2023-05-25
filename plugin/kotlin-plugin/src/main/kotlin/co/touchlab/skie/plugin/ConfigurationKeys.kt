package co.touchlab.skie.plugin

import co.touchlab.skie.util.directory.SkieDirectories
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import java.io.File

object ConfigurationKeys {

    val buildId = CompilerConfigurationKey<String>("SKIE build ID")
    // WIP Refactor to use SkieDirectories
    val skieBuildDir = CompilerConfigurationKey<File>("Directory with SKIE build")
    val skieDirectories = CompilerConfigurationKey<SkieDirectories>("SKIE directories")

    object SwiftCompiler {

        val swiftVersion = CompilerConfigurationKey<String>("Swift version")
        val parallelCompilation = CompilerConfigurationKey<Boolean>("Is Swift parallel compilation enabled")
        val additionalFlags = CompilerConfigurationKey<List<String>>("Additional Swift compiler flags")
    }
}
