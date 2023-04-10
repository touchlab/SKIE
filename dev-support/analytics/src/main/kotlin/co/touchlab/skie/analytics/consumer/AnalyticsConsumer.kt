package co.touchlab.skie.analytics.consumer

import co.touchlab.skie.dev_support.analytics.BuildConfig
import co.touchlab.skie.plugin.analytics.air.element.AirProject
import co.touchlab.skie.plugin.analytics.compiler.CompilerAnalytics
import co.touchlab.skie.plugin.analytics.producer.AnalyticsEncryptor
import co.touchlab.skie.plugin.analytics.producer.compressor.EfficientAnalyticsCompressor
import co.touchlab.skie.plugin.analytics.producer.compressor.FastAnalyticsCompressor
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.nio.file.Path
import kotlin.io.path.readBytes

private val resources = Path.of(BuildConfig.RESOURCES)

fun main() {
    val privateKey = Path.of("/Users/filip/private_key.der")

    println(loadJson<CompilerAnalytics>("d2d78262-6170-456d-be72-cbe385bf0bef.compiler.2", privateKey))
    println(loadString("d2d78262-6170-456d-be72-cbe385bf0bef.gradle.2", privateKey))
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

private val json = Json { prettyPrint = true }

private inline fun <reified T> loadJson(file: String, privateKeyPath: Path): String {
    val data = loadByteArray(file, privateKeyPath)

    val deserialized = Json.decodeFromString<T>(String(data))

    return json.encodeToString(deserialized)
}
