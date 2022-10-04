package co.touchlab.swiftpack.spec

import kotlinx.serialization.Serializable

@Serializable
data class KotlinTypeReference(val container: MemberParentReference, val typeName: String): MemberParentReference {
    fun child(name: String): KotlinTypeReference {
        return KotlinTypeReference(this, name)
    }

    companion object {
        operator fun invoke(fqdn: String): KotlinTypeReference {
            val parts = fqdn.split('.')
            val packageName = parts.dropLast(1).joinToString(".")
            val classNames = parts.last().split('$')

            val packageReference = KotlinPackageReference(packageName)
            val container = classNames.dropLast(1).fold(packageReference as MemberParentReference) { acc, className ->
                KotlinTypeReference(acc, className)
            }
            return KotlinTypeReference(container, classNames.last())
        }
    }
}
