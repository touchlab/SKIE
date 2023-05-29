package co.touchlab.skie.plugin.license

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.util.Environment
import co.touchlab.skie.util.directory.SkieBuildDirectory
import co.touchlab.skie.util.directory.SkieLicensesDirectory
import co.touchlab.skie.util.directory.util.initializedDirectory
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import java.nio.file.Path
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.Base64
import kotlin.io.path.createFile
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.notExists
import kotlin.io.path.readText

object SkieLicenseProvider {

    fun loadLicense(skieBuildDirectory: SkieBuildDirectory): SkieLicense {
        val licensePath = skieBuildDirectory.license.toPath()

        return loadLicense(licensePath)
    }

    fun loadLicense(license: Path): SkieLicense {
        val fileContent = license.readText()

        return parseJwt(fileContent)
    }

    fun getValidLicenseLocationOrRequestNew(requestData: SkieLicense.RequestData): Path =
        findValidLicenseLocationOrNull(requestData) ?: renewLicense(requestData)

    private fun findValidLicenseLocationOrNull(requestData: SkieLicense.RequestData): Path? =
        try {
            requestData.licensesDirectory
                .listDirectoryEntries()
                .mapNotNull { path ->
                    tryParseJwtWithValidation(path.readText(), requestData)
                        ?.let { license -> path to license }
                }
                .maxByOrNull { it.second.issuedAt }
                ?.first
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
        // WIP
        return SkieLicensesDirectory.directory.resolve("license.jwt").toPath().also {
            if (it.notExists()) {
                it.createFile()
            }
        }
    }

    private const val serverUrl = "https://license.skie.touchlab.dev"

    private fun tryParseJwtWithValidation(jwt: String, requestData: SkieLicense.RequestData): SkieLicense? =
        try {
            parseJwtWithValidation(jwt, requestData)
        } catch (_: SkieLicenseError) {
            null
        }

    private fun parseJwtWithValidation(jwt: String, requestData: SkieLicense.RequestData): SkieLicense {
        val license = parseJwt(jwt)

        // WIP
//        license.validate(requestData)

        return license
    }

    private fun parseJwt(jwt: String): SkieLicense {
        // WIP
        return SkieLicense(
            licenseKey = SkieLicense.Key(""),
            organizationId = "",
            licensedSkieVersion = "",
            supportedKotlinVersions = Regex(".*"),
            licensedHashedRootProjectDiskLocations = Regex(".*"),
            licensedHashedPlatformUUID = "",
            environment = Environment.Dev,
            configurationFromServer = SkieLicense.ConfigurationFromServer(Configuration(), Configuration()),
            serverMessagesForGradleConfig = SkieLicense.MessagesFromServer.ForGradleConfig(emptyList(), emptyList()),
            serverMessagesForCompiler = SkieLicense.MessagesFromServer.ForCompiler(emptyList(), emptyList(), emptyList()),
            issuedAt = Instant.now(),
            expiresAt = Instant.now(),
            renewAt = Instant.now(),
        )

        try {
            val verifiedJwt = jwtVerifier.verify(jwt)

            return SkieLicense(verifiedJwt)
        } catch (_: TokenExpiredException) {
            throw SkieLicenseError(
                "Local copy of license expired and needs to be renewed. Connect to the internet and rerun the build to download new license.",
            )
        } catch (_: JWTVerificationException) {
            throw SkieLicenseError(
                "SKIE license is not valid. " +
                    "Try connecting to the internet and rerun the build to download new license. " +
                    "If the issue persists, please contact support.",
            )
        }
    }

    private val jwtVerifier = JWT.require(Algorithm.RSA256(getPublicKey())).build()

    private fun getPublicKey(): RSAPublicKey {
        val keyFactory = KeyFactory.getInstance("RSA")

        val publicKeyBytes = Base64.getDecoder().decode(publicKeyPem)

        val spec = X509EncodedKeySpec(publicKeyBytes)

        return keyFactory.generatePublic(spec) as RSAPublicKey
    }

    private const val publicKeyPem: String =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAz5rJa1Q9kshv2fko5taO" +
            "P/CNEeLWiKTi8zpaEt+7mXvMstsjO3dzVsAiR4TnKjRuXqcXK/DtQ1v5zOPyk8GK" +
            "Qomo3ngkJsSy11jGNRnRT9hoBpZoJCQWlW4r1OWaR2CfgSb7W2lfYWyZ78Wtflzp" +
            "o9tCFmvxtdjktlCzS5ikAk/xHikSvAWvNNVrAxf8AkDAyvSJUaHOVFq3yiiMeSA2" +
            "aDhz1OW4b8IY3cnXeM1ElrPuGUgmXV11dOt0rcHvGs3yVJW1XJd8DH8lMaBV3jab" +
            "ZTk0lJfm5CSHj9AWIDA9/3d019U7Gb9+xQLrC7jMgKtFG42r0KJfk5vGAYmwl5ZK" +
            "BQIDAQAB"

    fun deleteOldLicenses() {
        SkieLicensesDirectory.directory.toPath()
            .listDirectoryEntries()
            .forEach {
                deleteOldLicense(it)
            }
    }

    private fun deleteOldLicense(directoryWithLicenses: Path) {
//        WIP
    }
}

val SkieLicense.RequestData.licensesDirectory: Path
    get() = SkieLicensesDirectory.directory.resolve(licenseKey.value).initializedDirectory().toPath()
