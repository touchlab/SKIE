package co.touchlab.skie.plugin.generator.internal.configuration

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.configuration.ConfigurationKey
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

context(Configuration)
fun <T> DeclarationDescriptor.getConfiguration(key: ConfigurationKey<T>): T =
    this@Configuration[DeclarationDescriptorConfigurationTarget(this@getConfiguration), key]
