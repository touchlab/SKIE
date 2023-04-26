package co.touchlab.skie.plugin.analytics.producer

import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.nio.file.Path
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.net.ssl.HttpsURLConnection
import kotlin.io.path.deleteIfExists
import kotlin.io.path.getLastModifiedTime
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readBytes

class AnalyticsUploader(private val analyticsCollector: AnalyticsCollector) {

    fun sendAllIfPossible(directory: Path) {
        try {
            sendAll(directory)

            deleteOldFiles(directory)
        } catch (e: Throwable) {
            handleUploadError(e)
        }
    }

    private fun sendAll(analyticsDirectory: Path) {
        analyticsDirectory.listDirectoryEntries()
            .let { AnalyticsArtifact.fromFilesDeduplicated(it) }
            .parallelStream()
            .forEach {
                uploadAndDeleteSafely(it, analyticsDirectory)
            }
    }

    private fun uploadAndDeleteSafely(analyticsArtifact: AnalyticsArtifact, analyticsDirectory: Path) {
        try {
            upload(analyticsArtifact, analyticsDirectory)

            analyticsArtifact.deleteFrom(analyticsDirectory)
        } catch (e: Throwable) {
            handleUploadError(e)
        }
    }

    private fun upload(analyticsArtifact: AnalyticsArtifact, analyticsDirectory: Path) {
        val connection = openUploadConnection(analyticsArtifact)

        sendData(connection, analyticsArtifact, analyticsDirectory)

        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw UploadException("Cannot upload analytics file: ${analyticsArtifact.fileName}. Error: $responseCode")
        }
    }

    private fun sendData(
        connection: HttpsURLConnection,
        analyticsArtifact: AnalyticsArtifact,
        analyticsDirectory: Path,
    ) {
        val file = analyticsArtifact.path(analyticsDirectory)
        val fileContent = file.readBytes()

        connection.outputStream.write(fileContent)

        connection.connect()
    }

    private fun openUploadConnection(analyticsArtifact: AnalyticsArtifact): HttpsURLConnection {
        val url = getS3Url(analyticsArtifact)

        val connection = URL(url).openConnection() as HttpsURLConnection
        connection.requestMethod = "PUT"
        connection.setRequestProperty("Content-Type", "application/octet-stream")
        connection.doOutput = true

        return connection
    }

    private fun getS3Url(analyticsArtifact: AnalyticsArtifact): String {
        val endpoint = "https://api.touchlab.dev/skie/analytics/upload-url/${analyticsArtifact.environment.name}/${analyticsArtifact.fileName}"

        val connection = URL(endpoint).openConnection() as HttpsURLConnection

        connection.connect()

        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw UploadException("Cannot obtain S3 upload URL for analytics file: ${analyticsArtifact.fileName}. Error: $responseCode")
        }

        return String(connection.inputStream.readAllBytes())
    }

    private fun deleteOldFiles(directory: Path) {
        val yesterday = Instant.now().minus(1, ChronoUnit.DAYS)

        directory.listDirectoryEntries()
            .filter { it.getLastModifiedTime().toInstant().isBefore(yesterday) }
            .forEach { it.deleteIfExists() }
    }

    private fun handleUploadError(e: Throwable) {
        if (e is UnknownHostException && !isInternetAvailable()) {
            return
        }

        analyticsCollector.logExceptionAndRethrowIfDev(e)
    }

    private fun isInternetAvailable(): Boolean =
        try {
            URL("https://www.google.com").openConnection().connect()

            true
        } catch (e: Throwable) {
            false
        }

    class UploadException(override val message: String) : RuntimeException()
}
