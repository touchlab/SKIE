package co.touchlab.swiftpack.spec

import kotlinx.serialization.Serializable

@Serializable
sealed interface CallableMemberReference {
    val parent: MemberParentReference
}
