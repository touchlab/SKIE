package co.touchlab.skie.configuration

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

context(SkieConfiguration)
fun <T> DeclarationDescriptor.getConfiguration(key: ConfigurationKey<T>): T =
    this@SkieConfiguration[DeclarationDescriptorConfigurationTarget(this@getConfiguration), key]
