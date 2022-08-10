package co.touchlab.swiftpack.spec

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File

@Serializable
data class SwiftPackModule(
    val name: String,
    val references: References,
    val files: List<TemplateFile>,
    val kobjcTransforms: KobjcTransforms,
) {
    companion object {
        private val json = Json {
            allowStructuredMapKeys = true
        }

        fun read(file: File): SwiftPackModule {
            return file.inputStream().use {
                json.decodeFromStream(serializer(), it)
            }
        }

        fun SwiftPackModule.write(file: File) {
            file.outputStream().use {
                json.encodeToStream(serializer(), this, it)
            }
        }
    }

    @Serializable
    data class TemplateFile(
        val name: String,
        val contents: String,
    )

    @Serializable
    data class References(
        val types: Map<SwiftPackReference, KotlinTypeReference>,
        val properties: Map<SwiftPackReference, KotlinPropertyReference>,
        val functions: Map<SwiftPackReference, KotlinFunctionReference>,
    )
}

val SWIFTPACK_KOTLIN_TYPE_PREFIX = "__swiftpack__type__"
val SWIFTPACK_KOTLIN_PROPERTY_PREFIX = "__swiftpack__prop__"
val SWIFTPACK_KOTLIN_FUNCTION_PREFIX = "__swiftpack__func__"

typealias SwiftPackReference = String

@Serializable
sealed interface MemberParentReference

fun MemberParentReference.property(name: String): KotlinPropertyReference {
    return KotlinPropertyReference(this, name)
}

fun MemberParentReference.function(name: String, vararg args: KotlinTypeReference): KotlinFunctionReference {
    return KotlinFunctionReference(this, name, args.toList())
}

@Serializable
sealed interface CallableMemberReference {
    val parent: MemberParentReference
}

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
