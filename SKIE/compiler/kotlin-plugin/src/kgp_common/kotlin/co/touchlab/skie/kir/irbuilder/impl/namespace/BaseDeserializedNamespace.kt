package co.touchlab.skie.kir.irbuilder.impl.namespace

import co.touchlab.skie.compilerinject.reflection.reflectedBy
import co.touchlab.skie.compilerinject.reflection.reflectors.DeserializedMemberScopeReflector
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.resolve.scopes.MemberScope

internal abstract class BaseDeserializedNamespace<D : DeclarationDescriptor> : BaseNamespace<D>() {

    protected fun MemberScope.addFunctionDescriptorToImpl(functionDescriptor: SimpleFunctionDescriptor) {
        val reflectedMemberScope = this.reflectedBy<DeserializedMemberScopeReflector>()
        val impl = reflectedMemberScope.reflectedImpl

        val functionName = functionDescriptor.name

        impl.functionNames.add(functionName)

        val cache = impl.reflectedFunctions.cache
        cache[functionName] = listOf(functionDescriptor) + (cache[functionName] ?: emptyList())
    }
}
