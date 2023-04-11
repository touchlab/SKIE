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
import java.nio.file.Path
import kotlin.io.path.readBytes

private val resources = Path.of(BuildConfig.RESOURCES)

fun main() {
    val privateKey = Path.of("")

    val buildId = ""

    try {
        println(loadJson<CompilerAnalytics>("$buildId.compiler.2", privateKey))
        println("--------")
    } catch (_: Throwable) {
    }

    try {
        println(loadString("$buildId.gradle.2", privateKey))
        println("--------")
    } catch (_: Throwable) {
    }

    try {
        println(loadString("$buildId.hw.2", privateKey))
        println("--------")
    } catch (_: Throwable) {
    }

    try {
        println(loadString("$buildId.performance.2", privateKey))
        println("--------")
    } catch (_: Throwable) {
    }

    try {
        println(loadString("$buildId.skie-configuration.2", privateKey))
        println("--------")
    } catch (_: Throwable) {
    }

    try {
        println(loadString("$buildId.sys.2", privateKey))
        println("--------")
    } catch (_: Throwable) {
    }

    try {
        val air = loadAir("$buildId.air.2", privateKey)
        return
    } catch (_: Throwable) {
    }
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
