package co.touchlab.swiftpack.spec

import kotlinx.serialization.Serializable

@Serializable
data class KotlinPropertyReference(override val parent: MemberParentReference, val propertyName: String): CallableMemberReference {
    companion object {
        private val regex = "^(?:(\\w+(?:\\.?\\w+)*)\\.)?(\\w*):(\\w+)$".toRegex()
        operator fun invoke(fqdn: String) {
            val match = requireNotNull(regex.matchEntire(fqdn)) { "Invalid reference: $fqdn" }

            println(match.groups)
        }
    }
}
