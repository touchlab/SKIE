package co.touchlab.swiftpack.spec.module

import co.touchlab.swiftpack.spec.reference.KotlinDeclarationReference
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File

@Serializable
data class SwiftPackModule(
    val name: Name,
    val templateVariables: List<SwiftTemplateVariable<*>>,
    val symbols: List<KotlinDeclarationReference<*>>,
    val files: List<TemplateFile>,
    val transforms: List<ApiTransform>,
) {
    fun namespaced(namespace: String): SwiftPackModule {
        return copy(
            name = Name.Namespaced(namespace = namespace, name = name),
        )
    }

    @Serializable
    data class TemplateFile(
        val name: String,
        val contents: String,
    )

    @Serializable
    sealed interface Name {
        @Serializable
        data class Simple(val name: String) : Name

        @Serializable
        data class Namespaced(val namespace: String, val name: Name): Name
    }

    data class Reference(
        val namespace: String,
        val moduleFile: File,
    )

    companion object {
        private val json = Json {
            allowStructuredMapKeys = true
            classDiscriminator = "@type"
        }

        fun read(file: File): SwiftPackModule {
            @OptIn(ExperimentalSerializationApi::class)
            return file.inputStream().use {
                json.decodeFromStream(serializer(), it)
            }
        }

        fun SwiftPackModule.write(file: File) {
            @OptIn(ExperimentalSerializationApi::class)
            file.outputStream().use {
                json.encodeToStream(serializer(), this, it)
            }
        }

        fun isSwiftPackModule(file: File): Boolean = file.isFile && file.extension == "swiftpack"

        fun moduleReferencesInDir(namespace: String, dir: File): List<Reference> {
            return dir.listFiles(::isSwiftPackModule)?.map { file ->
                Reference(namespace, file)
            } ?: emptyList()
        }
    }
}
