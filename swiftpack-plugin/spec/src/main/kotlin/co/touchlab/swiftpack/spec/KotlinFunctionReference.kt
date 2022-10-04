package co.touchlab.swiftpack.spec

import kotlinx.serialization.Serializable

@Serializable
data class KotlinFunctionReference(
    override val parent: MemberParentReference,
    val functionName: String,
    val parameterTypes: List<KotlinTypeReference>,
): CallableMemberReference {
    companion object {
        private val regex = "^(?:(\\w+(?:\\.?\\w+)*)\\.)?(\\w*):(\\w+)\\(([\\w:.$]*)\\)$".toRegex()

        operator fun invoke(fqdn: String) {
            val match = requireNotNull(regex.matchEntire(fqdn)) { "Invalid reference: $fqdn" }
            println(match.groups)
        }
    }
}
