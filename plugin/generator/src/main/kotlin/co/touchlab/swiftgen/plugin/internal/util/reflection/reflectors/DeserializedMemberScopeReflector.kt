package co.touchlab.swiftgen.plugin.internal.util.reflection.reflectors

import co.touchlab.swiftgen.plugin.internal.util.reflection.Reflector
import co.touchlab.swiftgen.plugin.internal.util.reflection.reflectedBy
import org.jetbrains.kotlin.resolve.scopes.MemberScope

internal class DeserializedMemberScopeReflector(
    override val instance: MemberScope,
) : Reflector("org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedMemberScope") {

    private val impl by declaredField<Any>()

    val reflectedImpl: DeserializedMemberScopeOptimizedImplementationReflector
        get() = impl.reflectedBy()
}
