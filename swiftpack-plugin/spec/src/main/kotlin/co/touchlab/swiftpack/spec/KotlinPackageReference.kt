package co.touchlab.swiftpack.spec

import kotlinx.serialization.Serializable

@Serializable
data class KotlinPackageReference(val packageName: String): MemberParentReference {
    fun child(childName: String): KotlinPackageReference {
        return if (packageName.isBlank()) {
            KotlinPackageReference(childName)
        } else {
            KotlinPackageReference("$packageName.$childName")
        }
    }

    fun type(typeName: String): KotlinTypeReference {
        return KotlinTypeReference(this, typeName)
    }

    companion object {
        val ROOT = KotlinPackageReference("")
    }
}
