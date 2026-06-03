package co.touchlab.skie.compilerinject.compilerplugin

import co.touchlab.skie.context.InitPhaseContext
import co.touchlab.skie.context.MainSkieContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import co.touchlab.skie.util.directory.SkieDirectories as SkieDirectoriesValue

object SkieConfigurationKeys {

    val InitPhaseContext: CompilerConfigurationKey<co.touchlab.skie.context.InitPhaseContext> =
        CompilerConfigurationKey<co.touchlab.skie.context.InitPhaseContext>("InitSkiePhaseContext")

    val MainContext: CompilerConfigurationKey<MainSkieContext> =
        CompilerConfigurationKey<MainSkieContext>("MainSkieContext")

    val SkieDirectories: CompilerConfigurationKey<SkieDirectoriesValue> =
        CompilerConfigurationKey<SkieDirectoriesValue>("SKIE directories")
}

var CompilerConfiguration.initPhaseContext: InitPhaseContext
    get() = getNotNull(SkieConfigurationKeys.InitPhaseContext)
    set(value) = put(SkieConfigurationKeys.InitPhaseContext, value)

var CompilerConfiguration.mainSkieContext: MainSkieContext
    get() = getNotNull(SkieConfigurationKeys.MainContext)
    set(value) = put(SkieConfigurationKeys.MainContext, value)
