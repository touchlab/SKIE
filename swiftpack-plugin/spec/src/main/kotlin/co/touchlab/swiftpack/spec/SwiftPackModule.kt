package co.touchlab.swiftpack.spec

import co.touchlab.swiftpack.spec.NameMangling.demangledClassName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File

@Serializable
data class SwiftPackModule(
    val name: String,
    val files: List<TemplateFile>,
    val kobjcTransforms: Set<KobjcTransform>,
) {
    companion object {
        private val json = Json

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
}

