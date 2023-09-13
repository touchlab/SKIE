package co.touchlab.skie.kir

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

interface DescriptorRegistrationScope : DescriptorProvider {

    fun registerExposedDescriptor(descriptor: DeclarationDescriptor)
}
