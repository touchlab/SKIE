package co.touchlab.skie.plugin.api

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.api.kotlin.MutableDescriptorProvider
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.konan.KonanConfig
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey
import org.jetbrains.kotlin.container.StorageComponentContainer

// TODO Merge with SkieCompilerConfigurationKey

object SkieContextKey : CompilerConfigurationKey<SkieContext>("SkieContext")

object SkieComponentContainerKey: CompilerConfigurationKey<StorageComponentContainer>("SkieComponentContainer")

object MutableDescriptorProviderKey : CompilerConfigurationKey<MutableDescriptorProvider>("MutableDescriptorProvider")

object DescriptorProviderKey : CompilerConfigurationKey<DescriptorProvider>("DescriptorProvider")

val CommonBackendContext.skieContext: SkieContext
    get() = configuration.skieContext

val CompilerConfiguration.skieComponents: StorageComponentContainer
    get() = getNotNull(SkieComponentContainerKey)

val KonanConfig.skieContext: SkieContext
    get() = configuration.skieContext

val CompilerConfiguration.skieContext: SkieContext
    get() = getNotNull(SkieContextKey)

val CommonBackendContext.descriptorProvider: DescriptorProvider
    get() = configuration.descriptorProvider

val CompilerConfiguration.descriptorProvider: DescriptorProvider
    get() = getNotNull(DescriptorProviderKey)

val CommonBackendContext.mutableDescriptorProvider: MutableDescriptorProvider
    get() = configuration.getNotNull(MutableDescriptorProviderKey)
