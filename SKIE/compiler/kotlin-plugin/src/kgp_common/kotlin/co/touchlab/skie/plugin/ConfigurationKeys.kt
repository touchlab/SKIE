package co.touchlab.skie.plugin

import co.touchlab.skie.util.directory.SkieDirectories
import org.jetbrains.kotlin.config.CompilerConfigurationKey

object ConfigurationKeys {

    val skieDirectories = CompilerConfigurationKey<SkieDirectories>("SKIE directories")

    object SwiftCompiler {

        val swiftVersion = CompilerConfigurationKey<String>("Swift version")
        val additionalFlags = CompilerConfigurationKey<List<String>>("Additional Swift compiler flags")
    }
}
