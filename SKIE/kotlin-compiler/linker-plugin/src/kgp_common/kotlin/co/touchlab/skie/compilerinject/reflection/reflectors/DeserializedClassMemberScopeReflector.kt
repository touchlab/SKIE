package co.touchlab.skie.compilerinject.reflection.reflectors

import co.touchlab.skie.compilerinject.reflection.Reflector
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.storage.NotNullLazyValue

class DeserializedClassMemberScopeReflector(override val instance: MemberScope) :
    Reflector("org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor\$DeserializedClassMemberScope") {

    val allDescriptors by declaredField<NotNullLazyValue<*>>()
}
