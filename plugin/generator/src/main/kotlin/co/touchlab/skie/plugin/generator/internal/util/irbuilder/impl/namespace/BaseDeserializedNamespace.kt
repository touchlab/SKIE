package co.touchlab.skie.plugin.generator.internal.util.irbuilder.impl.namespace

import co.touchlab.skie.plugin.api.kotlin.DescriptorProvider
import co.touchlab.skie.plugin.generator.internal.util.reflection.reflectedBy
import co.touchlab.skie.plugin.generator.internal.util.reflection.reflectors.DeserializedMemberScopeReflector
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.resolve.scopes.MemberScope

internal abstract class BaseDeserializedNamespace<D : DeclarationDescriptor>(
    descriptorProvider: DescriptorProvider,
) : BaseNamespace<D>(descriptorProvider) {

    protected fun MemberScope.addFunctionDescriptorToImpl(functionDescriptor: SimpleFunctionDescriptor) {
        val reflectedMemberScope = this.reflectedBy<DeserializedMemberScopeReflector>()
        val impl = reflectedMemberScope.reflectedImpl

        val functionName = functionDescriptor.name

        impl.functionNames.add(functionName)

        val cache = impl.reflectedFunctions.cache
        cache[functionName] = listOf(functionDescriptor) + (cache[functionName] ?: emptyList())
    }
}
