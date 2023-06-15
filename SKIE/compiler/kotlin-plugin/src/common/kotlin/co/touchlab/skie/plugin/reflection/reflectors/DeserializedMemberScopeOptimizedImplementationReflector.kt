package co.touchlab.skie.plugin.reflection.reflectors

import co.touchlab.skie.plugin.reflection.Reflector
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.storage.MemoizedFunctionToNotNull
import co.touchlab.skie.plugin.reflection.reflectedBy

class DeserializedMemberScopeOptimizedImplementationReflector(
    override val instance: Any,
) : Reflector("org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedMemberScope\$OptimizedImplementation") {

    private val functions by declaredField<MemoizedFunctionToNotNull<Name, Collection<SimpleFunctionDescriptor>>>()

    val reflectedFunctions: MapBasedMemoizedFunctionReflector<Name, Collection<SimpleFunctionDescriptor>>
        get() = functions.reflectedBy()

    val functionNames by declaredProperty<MutableSet<Name>>()
}
