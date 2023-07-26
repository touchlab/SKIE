package co.touchlab.skie.plugin.generator.internal.configuration

import co.touchlab.skie.plugin.api.configuration.SkieConfiguration
import co.touchlab.skie.configuration.ConfigurationKey
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

context(SkieConfiguration)
fun <T> DeclarationDescriptor.getConfiguration(key: ConfigurationKey<T>): T =
    this@SkieConfiguration[DeclarationDescriptorConfigurationTarget(this@getConfiguration), key]
