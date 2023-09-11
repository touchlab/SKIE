package co.touchlab.skie.compilerinject.reflection

import co.touchlab.skie.phases.SkieContext
import co.touchlab.skie.kir.DescriptorProvider
import co.touchlab.skie.kir.MutableDescriptorProvider
import co.touchlab.skie.util.directory.SkieDirectories
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

object ConfigurationKeys {

    val skieDirectories = CompilerConfigurationKey<SkieDirectories>("SKIE directories")

    object SwiftCompiler {

        val swiftVersion = CompilerConfigurationKey<String>("Swift version")
        val additionalFlags = CompilerConfigurationKey<List<String>>("Additional Swift compiler flags")
    }
}

object SkieContextKey : CompilerConfigurationKey<SkieContext>("SkieContext")

object MutableDescriptorProviderKey : CompilerConfigurationKey<MutableDescriptorProvider>("MutableDescriptorProvider")

object DescriptorProviderKey : CompilerConfigurationKey<DescriptorProvider>("DescriptorProvider")

val CommonBackendContext.skieContext: SkieContext
    get() = configuration.skieContext

val CompilerConfiguration.skieContext: SkieContext
    get() = getNotNull(SkieContextKey)

val CompilerConfiguration.descriptorProvider: DescriptorProvider
    get() = getNotNull(DescriptorProviderKey)

val CommonBackendContext.descriptorProvider: DescriptorProvider
    get() = configuration.descriptorProvider

val CommonBackendContext.mutableDescriptorProvider: MutableDescriptorProvider
    get() = configuration.getNotNull(MutableDescriptorProviderKey)
