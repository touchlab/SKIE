package co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.namespace

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.irbuilder.UnsupportedDeclarationDescriptorException
import co.touchlab.skie.plugin.generator.internal.util.reflection.reflectedBy
import co.touchlab.skie.plugin.generator.internal.util.reflection.reflectors.DeserializedClassMemberScopeReflector
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.ObsoleteDescriptorBasedAPI
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor

internal class DeserializedClassNamespace(
    override val descriptor: DeserializedClassDescriptor,
    descriptorProvider: DescriptorProvider,
) : BaseDeserializedNamespace<ClassDescriptor>(descriptorProvider) {

    override val sourceElement: SourceElement
        get() = descriptor.source

    override fun addDescriptorIntoDescriptorHierarchy(declarationDescriptor: DeclarationDescriptor) {
        when (declarationDescriptor) {
            is SimpleFunctionDescriptor -> addFunctionDescriptor(declarationDescriptor)
            is ClassConstructorDescriptor -> addSecondaryConstructorDescriptor(declarationDescriptor)
            else -> throw UnsupportedDeclarationDescriptorException(declarationDescriptor)
        }
    }

    private fun addFunctionDescriptor(functionDescriptor: SimpleFunctionDescriptor) {
        addDescriptorToAllDescriptors(functionDescriptor)
        descriptor.unsubstitutedMemberScope.addFunctionDescriptorToImpl(functionDescriptor)
    }

    private fun addDescriptorToAllDescriptors(declarationDescriptor: DeclarationDescriptor) {
        val classScope = descriptor.unsubstitutedMemberScope.reflectedBy<DeserializedClassMemberScopeReflector>()

        @Suppress("UNCHECKED_CAST")
        val children = classScope.allDescriptors() as ArrayList<DeclarationDescriptor>

        children.add(declarationDescriptor)
    }

    private fun addSecondaryConstructorDescriptor(constructorDescriptor: ClassConstructorDescriptor) {
        require(!constructorDescriptor.isPrimary) { "Primary constructors are not yet supported." }

        (descriptor.constructors as MutableCollection).add(constructorDescriptor)
    }

    @OptIn(ObsoleteDescriptorBasedAPI::class)
    override fun generateNamespaceIr(generatorContext: GeneratorContext): IrDeclarationContainer =
        generatorContext.symbolTable.referenceClass(descriptor).owner
}
