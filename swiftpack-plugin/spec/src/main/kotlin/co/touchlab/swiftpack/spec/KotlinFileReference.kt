package co.touchlab.swiftpack.spec

import kotlinx.serialization.Serializable

@Serializable
data class KotlinFileReference(val packageReference: KotlinPackageReference, val fileName: String) {
    companion object {
        operator fun invoke(path: String): KotlinFileReference {
            val parts = path.split("/")
            val packageName = parts.dropLast(1).joinToString(".")
            val fileName = parts.last()
            return KotlinFileReference(KotlinPackageReference(packageName), fileName)
        }
    }
}
