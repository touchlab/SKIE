package co.touchlab.skie.analytics.consumer

import co.touchlab.skie.dev.analytics.BuildConfig
import co.touchlab.skie.plugin.analytics.gradle.GradleAnalytics
import co.touchlab.skie.plugin.analytics.producer.AnalyticsArtifact
import co.touchlab.skie.plugin.analytics.producer.AnalyticsEncryptor
import co.touchlab.skie.plugin.analytics.producer.compressor.CompressionMethod
import co.touchlab.skie.plugin.analytics.producer.compressor.EfficientAnalyticsCompressor
import co.touchlab.skie.plugin.analytics.producer.compressor.FastAnalyticsCompressor
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import java.time.Instant
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readBytes

private val analyticsDir = Path.of(BuildConfig.ANALYTICS_DIR)

suspend fun main() {
    val privateKey = BuildConfig.ANALYTICS_PRIVATE_KEY.decodeBase64Bytes()

    val events = mutableListOf<MixpanelEvent>()

    // List analytics files
    val analyticsArtifacts = analyticsDir.listDirectoryEntries().flatMap { parentDir ->
        AnalyticsArtifact.fromFilesDeduplicated(parentDir.listDirectoryEntries()).map {
            AbsoluteAnalyticsArtifact(
                artifact = it,
                parentDir = parentDir,
            )
        }
    }

    analyticsArtifacts.forEach { artifact ->
        val gradle = loadJson<GradleAnalytics>(artifact, privateKey)

        println("Sending for build ${artifact.artifact.buildId} time ${Instant.ofEpochMilli(gradle.timestampInMs)}")
        events += MixpanelEvent(
            name = "skie-run",
            properties = MixpanelEvent.Properties(
                gradleVersion = gradle.gradleVersion,
                kotlinVersion = gradle.kotlinVersion,
                skieVersion = artifact.artifact.skieVersion,
                rootProjectName = gradle.rootProjectName,
                projectPath = gradle.projectPath,
                licenseKey = gradle.licenseKey,
                time = gradle.timestampInMs,
                organizationKey = gradle.organizationKey,
                buildId = artifact.artifact.buildId,
            )
        )
    }

    check(events.isNotEmpty()) { "No events to send to mixpanel!" }

    val client = HttpClient()
    val chunkedEvents = events.chunked(2000)

    chunkedEvents.forEachIndexed { index, chunk ->
        println("Sending chunk ${index + 1}/${chunkedEvents.size}")
        val response = client.post("https://api.mixpanel.com/import?strict=1&project_id=${BuildConfig.MIXPANEL_PROJECT}") {
            basicAuth(
                username = BuildConfig.MIXPANEL_USERNAME,
                password = BuildConfig.MIXPANEL_PASSWORD,
            )
            accept(ContentType.Application.Json)
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(ListSerializer(MixpanelEvent.serializer()), chunk))
        }
        val responseText = response.bodyAsText()
        println("Sent chunk ${index + 1}/${chunkedEvents.size} with response $responseText")
    }
}


private fun loadByteArray(artifact: AbsoluteAnalyticsArtifact, privateKey: ByteArray): ByteArray {
    val data = artifact.path.readBytes()

    val decryptedData = AnalyticsEncryptor.decrypt(data, privateKey)

    val decompressor = when (artifact.artifact.compressionMethod) {
        CompressionMethod.Deflate -> FastAnalyticsCompressor
        CompressionMethod.Bzip2 -> EfficientAnalyticsCompressor
    }

    return decompressor.decompress(decryptedData)
}

private inline fun <reified T> loadJson(artifact: AbsoluteAnalyticsArtifact, privateKey: ByteArray): T {
    val data = loadByteArray(artifact, privateKey)

    return Json.decodeFromString(String(data))
}

data class AbsoluteAnalyticsArtifact(
    val artifact: AnalyticsArtifact,
    val parentDir: Path,
) {
    val path: Path by lazy { artifact.path(parentDir) }
}

@Serializable
data class MixpanelEvent(
    @SerialName("event")
    val name: String,
    val properties: Properties
) {
    @Serializable
    data class Properties(
        @SerialName("Gradle Version")
        val gradleVersion: String,
        @SerialName("Kotlin Version")
        val kotlinVersion: String,
        @SerialName("SKIE Version")
        val skieVersion: String,
        @SerialName("Root Project Name")
        val rootProjectName: String,
        @SerialName("Project Path")
        val projectPath: String,
        @SerialName("License Key")
        val licenseKey: String,
        val time: Long,
        @SerialName("distinct_id")
        val organizationKey: String,
        @SerialName("\$insert_id")
        val buildId: String,
    )
}
