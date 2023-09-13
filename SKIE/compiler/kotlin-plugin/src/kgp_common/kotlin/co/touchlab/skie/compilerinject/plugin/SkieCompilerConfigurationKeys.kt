package co.touchlab.skie.compilerinject.plugin

import co.touchlab.skie.phases.context.MainSkieContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import co.touchlab.skie.util.directory.SkieDirectories as SkieDirectoriesValue

object SkieConfigurationKeys {

    object SkiePhaseContext : CompilerConfigurationKey<MainSkieContext>("MainSkieContext")

    object SkieDirectories : CompilerConfigurationKey<SkieDirectoriesValue>("SKIE directories")

    object SwiftCompiler {

        val swiftVersion = CompilerConfigurationKey<String>("Swift version")
        val additionalFlags = CompilerConfigurationKey<List<String>>("Additional Swift compiler flags")
    }
}

var CompilerConfiguration.mainSkieContext: MainSkieContext
    get() = getNotNull(SkieConfigurationKeys.SkiePhaseContext)
    set(value) = put(SkieConfigurationKeys.SkiePhaseContext, value)
