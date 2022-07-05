package co.touchlab.swiftpack.spec

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File

@Serializable
data class SwiftPackModule(
    val files: List<String>,
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
}
