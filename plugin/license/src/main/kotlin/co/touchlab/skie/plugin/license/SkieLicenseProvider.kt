package co.touchlab.skie.plugin.license

import co.touchlab.skie.plugin.license.util.JwtParser
import co.touchlab.skie.plugin.license.util.LicenseWithPath
import co.touchlab.skie.plugin.license.util.getLicensePaths
import co.touchlab.skie.util.BuildConfig
import co.touchlab.skie.util.directory.SkieBuildDirectory
import co.touchlab.skie.util.directory.SkieLicensesDirectory
import co.touchlab.skie.util.directory.util.initializedDirectory
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Path
import java.time.Instant
import javax.net.ssl.HttpsURLConnection
import kotlin.io.path.readText
import kotlin.io.path.writeText

object SkieLicenseProvider {

    fun loadLicense(skieBuildDirectory: SkieBuildDirectory): SkieLicense {
        val licensePath = skieBuildDirectory.license.toPath()

        return loadLicense(licensePath)
    }

    fun loadLicense(license: Path): SkieLicense {
        val fileContent = license.readText()

        return JwtParser.parseJwt(fileContent)
    }

    fun getValidLicenseLocationOrRequestNew(requestData: SkieLicense.RequestData): Path =
        findValidLicenseLocationOrNull(requestData) ?: renewLicense(requestData)

    private fun findValidLicenseLocationOrNull(requestData: SkieLicense.RequestData): Path? =
        try {
            requestData.licensesDirectory
                .getLicensePaths()
                .mapNotNull { path ->
                    JwtParser.tryParseJwtWithValidation(path.readText(), requestData)
                        ?.let { license -> LicenseWithPath(license, path) }
                }
                .maxByOrNull { it.license.issuedAt }
                ?.path
        } catch (_: SkieLicenseError) {
            null
        }

    fun tryToRenewLicense(requestData: SkieLicense.RequestData) {
        try {
            renewLicense(requestData)
        } catch (_: RuntimeException) {
        }
    }

    fun tryToRenewLicenseIfExpiring(requestData: SkieLicense.RequestData) {
        try {
            renewLicenseIfExpiring(requestData)
        } catch (_: RuntimeException) {
        }
    }

    private fun renewLicenseIfExpiring(requestData: SkieLicense.RequestData) {
        val licenseLocation = findValidLicenseLocationOrNull(requestData) ?: return

        val license = loadLicense(licenseLocation)

        if (license.needsToBeRenewed) {
            renewLicense(requestData)
        }
    }

    fun renewLicense(requestData: SkieLicense.RequestData): Path {
        val endpoint = getRenewLicenseEndpoint()

        val connection = createConnectionToLicensingServer(endpoint)

        connection.sendLicenseRenewalRequest(requestData)

        val responseBody = connection.getResponseBody()

        return saveLicenseToDisk(requestData, responseBody)
    }

    private fun createConnectionToLicensingServer(endpoint: String): HttpsURLConnection {
        val connection = URL(endpoint).openConnection() as HttpsURLConnection

        connection.requestMethod = "GET"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doInput = true
        connection.doOutput = true

        return connection
    }

    private fun HttpsURLConnection.sendLicenseRenewalRequest(requestData: SkieLicense.RequestData) {
        val serializedRequestData = Json.encodeToString(requestData)

        outputStream.write(serializedRequestData.toByteArray())

        connect()
    }

    private fun HttpsURLConnection.getResponseBody(): String {
        val responseCode = this.responseCode
        val responseBody = String(this.inputStream.readAllBytes())

        if (responseCode != HttpURLConnection.HTTP_OK) {
            // WIP Correct error handling
            throw SkieLicenseError("SKIE license couldn't be renewed because of a server error: $responseCode - $responseBody")
        }
        return responseBody
    }

    private fun saveLicenseToDisk(requestData: SkieLicense.RequestData, jwt: String): Path {
        val licensePath = requestData.licensesDirectory.resolve("${Instant.now().toEpochMilli()}.jwt")

        licensePath.writeText(jwt)

        return licensePath
    }

    // TODO Move to some config file
    private const val mainServerUrl = "https://license.skie.touchlab.dev"

    private fun getRenewLicenseEndpoint(): String {
        val serverUrl = System.getenv("SKIE_LICENSE_SERVER_URL") ?: mainServerUrl

        return "$serverUrl/api/${BuildConfig.SKIE_VERSION}/license"
    }
}

val SkieLicense.RequestData.licensesDirectory: Path
    get() = SkieLicensesDirectory.directory.resolve(licenseKey.value).initializedDirectory().toPath()
