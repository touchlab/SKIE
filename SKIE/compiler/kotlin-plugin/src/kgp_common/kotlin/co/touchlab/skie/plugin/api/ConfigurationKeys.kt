package co.touchlab.skie.plugin.api

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.MutableDescriptorProvider
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

// TODO Merge with SkieCompilerConfigurationKey

object SkieContextKey : CompilerConfigurationKey<SkieContext>("SkieContext")

object MutableDescriptorProviderKey : CompilerConfigurationKey<MutableDescriptorProvider>("MutableDescriptorProvider")

object DescriptorProviderKey : CompilerConfigurationKey<DescriptorProvider>("DescriptorProvider")

val CommonBackendContext.skieContext: SkieContext
    get() = configuration.skieContext

val CompilerConfiguration.skieContext: SkieContext
    get() = getNotNull(SkieContextKey)

val CommonBackendContext.descriptorProvider: DescriptorProvider
    get() = configuration.getNotNull(DescriptorProviderKey)

val CommonBackendContext.mutableDescriptorProvider: MutableDescriptorProvider
    get() = configuration.getNotNull(MutableDescriptorProviderKey)
