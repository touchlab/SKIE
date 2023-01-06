package co.touchlab.skie.plugin.api

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.config.CompilerConfigurationKey

object SkieContextKey : CompilerConfigurationKey<SkieContext>("SkieContext")

object DescriptorProviderKey : CompilerConfigurationKey<DescriptorProvider>("DescriptorProvider")

val CommonBackendContext.skieContext: SkieContext
    get() = configuration.getNotNull(SkieContextKey)

val CommonBackendContext.descriptorProvider: DescriptorProvider
    get() = configuration.getNotNull(DescriptorProviderKey)
