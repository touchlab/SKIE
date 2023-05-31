package co.touchlab.skie.plugin.license

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.plugin.license.util.getHashedPlatformUUID
import co.touchlab.skie.util.BuildConfig
import co.touchlab.skie.util.Environment
import com.auth0.jwt.interfaces.Payload
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.time.Instant

class SkieLicense(
    val licenseKey: Key,
    val organizationId: String,
    val licensedSkieVersion: String,
    val supportedKotlinVersions: Regex,
    val licensedHashedRootProjectDiskLocations: Regex,
    val licensedHashedPlatformUUID: String,
    val environment: Environment,
    val configurationFromServer: ConfigurationFromServer,
    val serverMessagesForGradleConfig: MessagesFromServer.ForGradleConfig,
    val serverMessagesForCompiler: MessagesFromServer.ForCompiler,
    val issuedAt: Instant,
    val expiresAt: Instant,
    val renewAt: Instant,
) {

    fun validate(requestData: RequestData) {
        verify(licenseKey == requestData.licenseKey) {
            "Project license key (${requestData.licenseKey}) does not match key in the loaded license (${licenseKey}). " +
                "This is most likely a bug in SKIE Gradle plugin. " +
                "Please try to run `./gradlew clean` and then rerun the build while being connected to internet to download new license."
        }
        verify(supportedKotlinVersions.matches(requestData.kotlinVersion)) {
            "Kotlin version ${requestData.kotlinVersion} is not compatible with SKIE version ${BuildConfig.SKIE_VERSION}. " +
                "Please refer to the SKIE documentation (https://skie.touchlab.co/Installation) for more information about the versions compatibility."
        }
        verify(licensedHashedRootProjectDiskLocations.matches(requestData.hashedRootProjectDiskLocation)) {
            "License with key ${requestData.licenseKey} is not valid for this project. " +
                "This issue might have been caused by changing the location of the root directory (or by renaming it). " +
                "Please try to run `./gradlew clean` and then rerun the build while being connected to internet to download new license. " +
                "If the issue persists, please contact support."
        }

        validateWithoutFullContext()
    }

    fun validateWithoutFullContext() {
        // WIP
        return

        verify(licensedSkieVersion == BuildConfig.SKIE_VERSION) {
            "License with key $licenseKey is not valid for this SKIE version (${BuildConfig.SKIE_VERSION}). " +
                "Correct version: ${licensedSkieVersion}. " +
                "This is most likely a bug in SKIE Gradle plugin. " +
                "Please try to run `./gradlew clean` and then rerun the build while being connected to internet to download new license. " +
                "If the issue persists, please contact support."
        }
        verify(licensedHashedPlatformUUID == getHashedPlatformUUID()) {
            "SKIE license was not issued for this computer. " +
                "This issue might be caused by copying the license to another computer. " +
                "Please try to run `./gradlew clean` and then rerun the build while being connected to internet to download new license. " +
                "If the issue persists, please contact support."
        }
        verify(expiresAt.isBefore(Instant.now())) {
            "Local copy of license expired and needs to be renewed. Connect to the internet and rerun the build to download new license."
        }
    }

    private fun verify(condition: Boolean, errorMessage: () -> String) {
        if (!condition) {
            throw SkieLicenseError(errorMessage())
        }
    }

    val needsToBeRenewed: Boolean
        get() = renewAt.isBefore(Instant.now())

    @JvmInline
    @Serializable
    value class Key(val value: String)

    object MessagesFromServer {

        @Serializable
        data class ForGradleConfig(
            val warnings: List<String>,
            val info: List<String>,
        )

        @Serializable
        data class ForCompiler(
            val errors: List<String>,
            val warnings: List<String>,
            val info: List<String>,
        )
    }

    @Serializable
    data class ConfigurationFromServer(
        val defaultConfiguration: Configuration,
        val enforcedConfiguration: Configuration,
    )

    @Serializable
    data class RequestData(
        val buildId: String,
        val licenseKey: Key,
        val skieVersion: String,
        val kotlinVersion: String,
        val isCI: Boolean,
        val hashedRootProjectDiskLocation: String,
        val hashedPlatformUUID: String,
        val hashedGitRemotes: List<String>,
    )

    @Serializable
    data class JwtBody(
        val licenseKey: Key,
        val organizationId: String,
        val licensedSkieVersion: String,
        val supportedKotlinVersionsRegex: String,
        val licensedHashedRootProjectDiskLocationsRegex: String,
        val licensedHashedPlatformUUID: String,
        val environment: Environment,
        val configurationFromServer: ConfigurationFromServer,
        val serverMessagesForGradleConfigPhase: MessagesFromServer.ForGradleConfig,
        val serverMessagesForCompiler: MessagesFromServer.ForCompiler,
        val renewAtTimestamp: Long,
    )

    fun toJwtBody(): JwtBody =
        JwtBody(
            licenseKey = licenseKey,
            organizationId = organizationId,
            licensedSkieVersion = licensedSkieVersion,
            supportedKotlinVersionsRegex = supportedKotlinVersions.pattern,
            licensedHashedRootProjectDiskLocationsRegex = licensedHashedRootProjectDiskLocations.pattern,
            licensedHashedPlatformUUID = licensedHashedPlatformUUID,
            environment = environment,
            configurationFromServer = configurationFromServer,
            serverMessagesForGradleConfigPhase = serverMessagesForGradleConfig,
            serverMessagesForCompiler = serverMessagesForCompiler,
            renewAtTimestamp = renewAt.epochSecond,
        )

    companion object {

        operator fun invoke(jwt: Payload): SkieLicense {
            val body = jwt.getClaim("body").asMap()

            val serializedBody = ObjectMapper().writeValueAsString(body)

            val jwtBody = Json.decodeFromString<JwtBody>(serializedBody)

            return SkieLicense(
                licenseKey = jwtBody.licenseKey,
                organizationId = jwtBody.organizationId,
                licensedSkieVersion = jwtBody.licensedSkieVersion,
                supportedKotlinVersions = Regex(jwtBody.supportedKotlinVersionsRegex),
                licensedHashedRootProjectDiskLocations = Regex(jwtBody.licensedHashedRootProjectDiskLocationsRegex),
                licensedHashedPlatformUUID = jwtBody.licensedHashedPlatformUUID,
                environment = jwtBody.environment,
                configurationFromServer = jwtBody.configurationFromServer,
                serverMessagesForGradleConfig = jwtBody.serverMessagesForGradleConfigPhase,
                serverMessagesForCompiler = jwtBody.serverMessagesForCompiler,
                issuedAt = Instant.ofEpochSecond(jwt.issuedAt.time),
                expiresAt = Instant.ofEpochSecond(jwt.expiresAt.time),
                renewAt = Instant.ofEpochSecond(jwtBody.renewAtTimestamp),
            )
        }
    }
}
