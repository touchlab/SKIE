package co.touchlab.swiftgen.plugin.internal.util.irbuilder

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor

internal class UnsupportedDeclarationDescriptorException(
    declarationDescriptor: DeclarationDescriptor,
) : IllegalArgumentException("Unsupported declaration descriptor type: ${declarationDescriptor::class}.")
