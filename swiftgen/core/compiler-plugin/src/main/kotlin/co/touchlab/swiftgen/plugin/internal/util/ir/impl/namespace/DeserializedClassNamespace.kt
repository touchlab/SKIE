package co.touchlab.swiftgen.plugin.internal.util.ir.impl.namespace

import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectedBy
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.DeserializedClassMemberScopeReflector
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.DeserializedMemberScopeReflector
import org.jetbrains.kotlin.descriptors.ClassConstructorDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor

internal class DeserializedClassNamespace(override val descriptor: DeserializedClassDescriptor) : BaseNamespace<ClassDescriptor>() {

    override val sourceElement: SourceElement
        get() = descriptor.source

    override fun addDescriptor(declarationDescriptor: DeclarationDescriptor) {
        when (declarationDescriptor) {
            is SimpleFunctionDescriptor -> addFunctionDescriptor(declarationDescriptor)
            is ClassConstructorDescriptor -> addSecondaryConstructorDescriptor(declarationDescriptor)
        }
    }

    private fun addFunctionDescriptor(functionDescriptor: SimpleFunctionDescriptor) {
        addDescriptorToAllDescriptors(functionDescriptor)
        addFunctionDescriptorToImpl(functionDescriptor)
    }

    private fun addDescriptorToAllDescriptors(declarationDescriptor: DeclarationDescriptor) {
        val classScope = descriptor.unsubstitutedMemberScope.reflectedBy<DeserializedClassMemberScopeReflector>()

        @Suppress("UNCHECKED_CAST")
        val children = classScope.allDescriptors() as ArrayList<DeclarationDescriptor>

        children.add(declarationDescriptor)
    }

    private fun addFunctionDescriptorToImpl(functionDescriptor: SimpleFunctionDescriptor) {
        val classScope = descriptor.unsubstitutedMemberScope.reflectedBy<DeserializedMemberScopeReflector>()
        val impl = classScope.reflectedImpl

        val functionName = functionDescriptor.name

        impl.functionNames.add(functionName)

        val cache = impl.reflectedFunctions.cache
        cache[functionName] = listOf(functionDescriptor) + (cache[functionName] ?: emptyList())
    }

    private fun addSecondaryConstructorDescriptor(constructorDescriptor: ClassConstructorDescriptor) {
        require(!constructorDescriptor.isPrimary) { "Primary constructors are not yet supported." }

        (descriptor.constructors as MutableCollection).add(constructorDescriptor)
    }

    override fun generateNamespaceIr(generatorContext: GeneratorContext): IrDeclarationContainer =
        generatorContext.symbolTable.referenceClass(descriptor).owner
}
