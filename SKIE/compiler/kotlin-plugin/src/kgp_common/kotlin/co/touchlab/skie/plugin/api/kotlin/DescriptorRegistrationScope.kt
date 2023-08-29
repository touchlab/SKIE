package co.touchlab.skie.plugin.api.kotlin

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

interface DescriptorRegistrationScope : DescriptorProvider {

    fun registerExposedDescriptor(descriptor: DeclarationDescriptor)
}
