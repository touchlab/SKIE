package co.touchlab.swiftgen.plugin.internal.util.irbuilder.impl.namespace

import co.touchlab.swiftgen.plugin.internal.util.irbuilder.UnsupportedDeclarationDescriptorException
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectedBy
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.DeserializedClassMemberScopeReflector
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor

internal class DeserializedClassNamespace(override val descriptor: DeserializedClassDescriptor) :
    BaseDeserializedNamespace<ClassDescriptor>() {

    override val sourceElement: SourceElement
        get() = descriptor.source

    override fun addDescriptor(declarationDescriptor: DeclarationDescriptor) {
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

    override fun generateNamespaceIr(generatorContext: GeneratorContext): IrDeclarationContainer =
        generatorContext.symbolTable.referenceClass(descriptor).owner
}
