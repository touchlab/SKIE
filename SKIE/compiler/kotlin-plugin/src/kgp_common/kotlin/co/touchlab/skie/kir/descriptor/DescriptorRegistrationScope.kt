package co.touchlab.skie.kir.descriptor

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

interface DescriptorRegistrationScope : DescriptorProvider {

    fun registerExposedDescriptor(descriptor: DeclarationDescriptor)
}
