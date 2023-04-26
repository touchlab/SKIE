package co.touchlab.skie.analytics.consumer

import co.touchlab.skie.dev_support.analytics.BuildConfig
import co.touchlab.skie.plugin.analytics.air.element.AirProject
import co.touchlab.skie.plugin.analytics.compiler.CompilerAnalytics
import co.touchlab.skie.plugin.analytics.producer.AnalyticsArtifact
import co.touchlab.skie.plugin.analytics.producer.AnalyticsEncryptor
import co.touchlab.skie.plugin.analytics.producer.compressor.CompressionMethod
import co.touchlab.skie.plugin.analytics.producer.compressor.EfficientAnalyticsCompressor
import co.touchlab.skie.plugin.analytics.producer.compressor.FastAnalyticsCompressor
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readBytes

private val resources = Path.of(BuildConfig.RESOURCES)

fun main() {
    val privateKey = Path.of("/Users/filip/private_key.der")

    val buildId = "d3cc9920-1712-44eb-b9bc-8e9f9e95a917"
    val skieVersion = "1.0.0-SNAPSHOT"
    val environment = "Dev"

    loadAll(buildId, skieVersion, environment, privateKey)
}

private fun loadAll(buildId: String, skieVersion: String, environment: String, privateKey: Path) {
    try {
        println(loadJson<CompilerAnalytics>(buildId, "compiler", skieVersion, environment, privateKey))
        println("--------")
    } catch (_: Throwable) {
    }

    try {
        println(loadString(buildId, "gradle", skieVersion, environment, privateKey))
        println("--------")
    } catch (_: Throwable) {
    }

    try {
        println(loadString(buildId, "hw", skieVersion, environment, privateKey))
        println("--------")
    } catch (_: Throwable) {
    }

    try {
        println(loadString(buildId, "performance", skieVersion, environment, privateKey))
        println("--------")
    } catch (_: Throwable) {
    }

    try {
        println(loadString(buildId, "skie-configuration", skieVersion, environment, privateKey))
        println("--------")
    } catch (_: Throwable) {
    }

    try {
        println(loadString(buildId, "sys", skieVersion, environment, privateKey))
        println("--------")
    } catch (_: Throwable) {
    }

    try {
        val air = loadAir(buildId, "air", skieVersion, environment, privateKey)
        return
    } catch (_: Throwable) {
    }
}

private fun loadByteArray(buildId: String, type: String, skieVersion: String, environment: String, privateKey: Path): ByteArray {
    val baseFileName = AnalyticsArtifact.fileNameWithoutCompressionName(
        buildId = buildId,
        type = type,
        skieVersion = skieVersion,
        environment = environment,
    )
    val artifactPaths = resources.listDirectoryEntries().filter { it.name.startsWith(baseFileName) }

    val artifact = AnalyticsArtifact.fromFilesDeduplicated(artifactPaths).first()

    val data = artifact.path(resources).readBytes()

    val decryptedData = AnalyticsEncryptor.decrypt(data, privateKey)

    val decompressor = when (artifact.compressionMethod) {
         CompressionMethod.Deflate -> FastAnalyticsCompressor
         CompressionMethod.Bzip2 -> EfficientAnalyticsCompressor
    }

    return decompressor.decompress(decryptedData)
}

private fun loadAir(buildId: String, type: String, skieVersion: String, environment: String, privateKey: Path): AirProject {
    val data = loadByteArray(buildId, type, skieVersion, environment, privateKey)

    val json = Json { classDiscriminator = "jsonType" }

    return json.decodeFromString(String(data))
}

private fun loadString(buildId: String, type: String, skieVersion: String, environment: String, privateKey: Path): String {
    val data = loadByteArray(buildId, type, skieVersion, environment, privateKey)

    return String(data)
}

private val json = Json { prettyPrint = true }

private inline fun <reified T> loadJson(buildId: String, type: String, skieVersion: String, environment: String, privateKey: Path): String {
    val data = loadByteArray(buildId, type, skieVersion, environment, privateKey)

    val deserialized = Json.decodeFromString<T>(String(data))

    return json.encodeToString(deserialized)
}
