package co.touchlab.skie.kir

import co.touchlab.skie.kir.DescriptorProvider
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

interface DescriptorRegistrationScope : DescriptorProvider {

    fun registerExposedDescriptor(descriptor: DeclarationDescriptor)
}
