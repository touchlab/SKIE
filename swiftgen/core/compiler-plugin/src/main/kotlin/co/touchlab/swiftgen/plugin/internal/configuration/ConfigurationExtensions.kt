package co.touchlab.swiftgen.plugin.internal.configuration

import co.touchlab.swiftgen.configuration.Configuration
import co.touchlab.swiftgen.configuration.ConfigurationKey
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

context(Configuration)
fun <T> DeclarationDescriptor.getConfiguration(key: ConfigurationKey<T>): T =
    this@Configuration[DeclarationDescriptorConfigurationTarget(this@getConfiguration), key]
