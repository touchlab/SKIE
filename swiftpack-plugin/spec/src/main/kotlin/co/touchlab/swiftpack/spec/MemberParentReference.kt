package co.touchlab.swiftpack.spec

import kotlinx.serialization.Serializable

@Serializable
sealed interface MemberParentReference

fun MemberParentReference.property(name: String): KotlinPropertyReference {
    return KotlinPropertyReference(this, name)
}

fun MemberParentReference.function(name: String, vararg args: KotlinTypeReference): KotlinFunctionReference {
    return KotlinFunctionReference(this, name, args.toList())
}
