package co.touchlab.skie.kir.irbuilder

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

class UnsupportedDeclarationDescriptorException(
    declarationDescriptor: DeclarationDescriptor,
) : IllegalArgumentException("Unsupported declaration descriptor type: ${declarationDescriptor::class}.")
