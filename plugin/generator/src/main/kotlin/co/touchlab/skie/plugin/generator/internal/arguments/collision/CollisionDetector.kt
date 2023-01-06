package co.touchlab.skie.plugin.generator.internal.arguments.collision

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.isInterface
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter
import org.jetbrains.kotlin.resolve.scopes.getDescriptorsFiltered

internal class CollisionDetector(descriptorProvider: DescriptorProvider) {

    private val selectors = mutableSetOf<String>()

    init {
        registerConstructors(descriptorProvider)
        registerClassMethods(descriptorProvider)
        registerExtensions(descriptorProvider)
        registerGlobalFunctions(descriptorProvider)
    }

    private fun registerConstructors(descriptorProvider: DescriptorProvider) {
        descriptorProvider.exportedClassDescriptors
            .flatMap { it.constructors }
            .filter { descriptorProvider.shouldBeExposed(it) }
            .forEach { register(it) }
    }

    private fun registerClassMethods(descriptorProvider: DescriptorProvider) {
        descriptorProvider.exportedClassDescriptors
            .filter { !it.kind.isInterface }
            .flatMap {
                it.unsubstitutedMemberScope.getDescriptorsFiltered(DescriptorKindFilter.FUNCTIONS)
            }
            .filterIsInstance<FunctionDescriptor>()
            .filter { descriptorProvider.shouldBeExposed(it) }
            .forEach { register(it) }
    }

    private fun registerExtensions(descriptorProvider: DescriptorProvider) {
        descriptorProvider.exportedCategoryMembersCallableDescriptors
            .filterIsInstance<FunctionDescriptor>()
            .forEach { register(it) }
    }

    private fun registerGlobalFunctions(descriptorProvider: DescriptorProvider) {
        descriptorProvider.exportedTopLevelCallableDescriptors
            .filterIsInstance<FunctionDescriptor>()
            .forEach { register(it) }
    }

    private fun register(functionDescriptor: FunctionDescriptor) {
        val functionSignature = functionDescriptor.toFunctionSignature()

        val selector = functionSignature.overloadResolutionSelector

        selectors.add(selector)
    }

    fun createsCollision(functionSignature: FunctionSignature): Boolean =
        functionSignature.overloadResolutionSelector in selectors
}
