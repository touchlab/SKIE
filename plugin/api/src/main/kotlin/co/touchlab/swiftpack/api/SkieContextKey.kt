package co.touchlab.swiftpack.api

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.config.CompilerConfigurationKey

object SkieContextKey: CompilerConfigurationKey<SkieContext>("SkieContext")

val CommonBackendContext.skieContext: SkieContext
    get() = configuration.getNotNull(SkieContextKey)
