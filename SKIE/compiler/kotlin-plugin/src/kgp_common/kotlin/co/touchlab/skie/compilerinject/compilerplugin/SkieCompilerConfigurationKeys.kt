package co.touchlab.skie.compilerinject.compilerplugin

import co.touchlab.skie.context.MainSkieContext
import co.touchlab.skie.phases.InitPhase
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import co.touchlab.skie.util.directory.SkieDirectories as SkieDirectoriesValue

object SkieConfigurationKeys {

    object InitPhaseContext : CompilerConfigurationKey<InitPhase.Context>("InitSkiePhaseContext")

    object MainContext : CompilerConfigurationKey<MainSkieContext>("MainSkieContext")

    object SkieDirectories : CompilerConfigurationKey<SkieDirectoriesValue>("SKIE directories")

    object SwiftCompiler {

        val swiftVersion = CompilerConfigurationKey<String>("Swift version")
        val additionalFlags = CompilerConfigurationKey<List<String>>("Additional Swift compiler flags")
    }
}

var CompilerConfiguration.initPhaseContext: InitPhase.Context
    get() = getNotNull(SkieConfigurationKeys.InitPhaseContext)
    set(value) = put(SkieConfigurationKeys.InitPhaseContext, value)

var CompilerConfiguration.mainSkieContext: MainSkieContext
    get() = getNotNull(SkieConfigurationKeys.MainContext)
    set(value) = put(SkieConfigurationKeys.MainContext, value)
