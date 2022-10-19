package co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors

import co.touchlab.swiftgen.plugin.internal.util.reflection.Reflector
import org.jetbrains.kotlin.resolve.scopes.MemberScope
import org.jetbrains.kotlin.storage.NotNullLazyValue

internal class DeserializedClassMemberScopeReflector(
    override val instance: MemberScope,
) : Reflector("org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor\$DeserializedClassMemberScope") {

    val allDescriptors by declaredField<NotNullLazyValue<*>>()
}
