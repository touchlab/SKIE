package co.touchlab.swiftgen.plugin.internal.util.ir.impl.namespace

import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectedBy
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.DeserializedClassMemberScopeReflector
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.DeserializedMemberScopeOptimizedImplementationReflector
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors.DeserializedMemberScopeReflector
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.descriptors.SourceElement
import org.jetbrains.kotlin.ir.declarations.IrDeclarationContainer
import org.jetbrains.kotlin.psi2ir.generators.GeneratorContext
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor

internal class DeserializedClassNamespace(override val descriptor: DeserializedClassDescriptor) : BaseNamespace() {

    override val sourceElement: SourceElement
        get() = descriptor.source

    override fun addDescriptor(declarationDescriptor: DeclarationDescriptor) {
        addDescriptorToAllDescriptors(declarationDescriptor)
        addDescriptorToImpl(declarationDescriptor)
    }

    private fun addDescriptorToAllDescriptors(declarationDescriptor: DeclarationDescriptor) {
        val classScope = descriptor.unsubstitutedMemberScope.reflectedBy<DeserializedClassMemberScopeReflector>()

        @Suppress("UNCHECKED_CAST")
        val children = classScope.allDescriptors() as ArrayList<DeclarationDescriptor>

        children.add(declarationDescriptor)
    }

    private fun addDescriptorToImpl(declarationDescriptor: DeclarationDescriptor) {
        val classScope = descriptor.unsubstitutedMemberScope.reflectedBy<DeserializedMemberScopeReflector>()
        val impl = classScope.reflectedImpl

        when (declarationDescriptor) {
            is SimpleFunctionDescriptor -> addFunctionDescriptorToImpl(declarationDescriptor, impl)
            else -> throw NotImplementedError()
        }
    }

    private fun addFunctionDescriptorToImpl(
        functionDescriptor: SimpleFunctionDescriptor,
        impl: DeserializedMemberScopeOptimizedImplementationReflector,
    ) {
        val functionName = functionDescriptor.name

        impl.functionNames.add(functionName)

        val cache = impl.reflectedFunctions.cache
        cache[functionName] = listOf(functionDescriptor) + (cache[functionName] ?: emptyList())
    }

    override fun generateNamespaceIr(generatorContext: GeneratorContext): IrDeclarationContainer =
        generatorContext.symbolTable.referenceClass(descriptor).owner
}
