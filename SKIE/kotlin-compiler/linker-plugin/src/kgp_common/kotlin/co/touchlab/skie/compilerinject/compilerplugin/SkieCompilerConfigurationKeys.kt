package co.touchlab.skie.compilerinject.compilerplugin

import co.touchlab.skie.context.InitPhaseContext
import co.touchlab.skie.context.MainSkieContext
import co.touchlab.skie.util.directory.SkieDirectories as SkieDirectoriesValue
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

object SkieConfigurationKeys {

    object InitPhaseContext : CompilerConfigurationKey<co.touchlab.skie.context.InitPhaseContext>("InitSkiePhaseContext")

    object MainContext : CompilerConfigurationKey<MainSkieContext>("MainSkieContext")

    object SkieDirectories : CompilerConfigurationKey<SkieDirectoriesValue>("SKIE directories")
}

var CompilerConfiguration.initPhaseContext: InitPhaseContext
    get() = getNotNull(SkieConfigurationKeys.InitPhaseContext)
    set(value) = put(SkieConfigurationKeys.InitPhaseContext, value)

var CompilerConfiguration.mainSkieContext: MainSkieContext
    get() = getNotNull(SkieConfigurationKeys.MainContext)
    set(value) = put(SkieConfigurationKeys.MainContext, value)
