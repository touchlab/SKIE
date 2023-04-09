package co.touchlab.skie.analytics.consumer

import co.touchlab.skie.dev_support.analytics.BuildConfig
import co.touchlab.skie.plugin.analytics.air.element.AirProject
import co.touchlab.skie.plugin.analytics.producer.AnalyticsEncryptor
import co.touchlab.skie.plugin.analytics.producer.compressor.EfficientAnalyticsCompressor
import co.touchlab.skie.plugin.analytics.producer.compressor.FastAnalyticsCompressor
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.readBytes

private val resources = Path.of(BuildConfig.RESOURCES)

fun main() {
}

private fun loadByteArray(file: String, privateKeyPath: Path): ByteArray {
    val path = resources.resolve(file)
    val data = path.readBytes()

    val decryptedData = AnalyticsEncryptor.decrypt(data, privateKeyPath)

    val decompressor = when {
        file.endsWith(".1") -> FastAnalyticsCompressor
        file.endsWith(".2") -> EfficientAnalyticsCompressor
        else -> error("Unknown compression for file: $file")
    }

    return decompressor.decompress(decryptedData)
}

private fun loadAir(file: String, privateKeyPath: Path): AirProject {
    val data = loadByteArray(file, privateKeyPath)

    val json = Json { classDiscriminator = "jsonType" }

    return json.decodeFromString(String(data))
}

private fun loadString(file: String, privateKeyPath: Path): String {
    val data = loadByteArray(file, privateKeyPath)

    return String(data)
}
