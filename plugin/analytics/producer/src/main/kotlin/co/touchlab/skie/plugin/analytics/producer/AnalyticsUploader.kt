package co.touchlab.skie.plugin.analytics.producer

import java.net.HttpURLConnection
import java.net.URL
import java.net.UnknownHostException
import java.nio.file.Path
import javax.net.ssl.HttpsURLConnection
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readBytes

object AnalyticsUploader {

    fun sendAllIfPossible(directory: Path) {
        try {
            sendAll(directory)
        } catch (e: Throwable) {
            if (e is UnknownHostException && !isInternetAvailable()) {
                return
            }

            // TODO Log to bugsnag
        }
    }

    private fun sendAll(directory: Path) {
        directory.listDirectoryEntries()
            .groupVersions()
            .parallelStream()
            .forEach {
                uploadAndDelete(it)
            }
    }

    private fun List<Path>.groupVersions(): List<FileWithMultipleVersions> =
        this.groupBy { FileWithMultipleVersions.nameWithoutVersion(it) }
            .map { FileWithMultipleVersions(it.value) }

    private fun uploadAndDelete(fileWithMultipleVersions: FileWithMultipleVersions) {
        upload(fileWithMultipleVersions.newest)

        fileWithMultipleVersions.deleteAll()
    }

    private fun upload(file: Path) {
        val fileContent = file.readBytes()

        val connection = URL("https://api.touchlab.dev/skie/irupload/${file.name}").openConnection() as HttpsURLConnection
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/octet-stream")
        connection.doOutput = true
        connection.outputStream.write(fileContent)

        connection.connect()

        val responseCode = connection.responseCode
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw UploadException("Cannot upload analytics. Error: $responseCode")
        }
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
