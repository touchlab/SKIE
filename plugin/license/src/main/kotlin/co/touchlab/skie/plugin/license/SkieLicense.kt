package co.touchlab.skie.plugin.license

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.util.hashed
import co.touchlab.skie.util.Environment
import kotlinx.serialization.Serializable
import java.time.Instant


@Serializable
data class SkieLicense(
    val licenseKey: Key,
    val organizationId: String,
    val licensedSkieVersion: SkieVersion,
    val supportedKotlinVersions: SerializableRegex,
    val licensedHashedRootProjectDiskLocations: SerializableRegex,
    val licensedHashedPlatformUUID: String,
    val environment: Environment,
    val configurationFromServer: ConfigurationFromServer,
    val serverMessagesForGradleConfigPhase: MessagesFromServer,
    val serverMessagesForCompiler: MessagesFromServer,
    // Instant::toEpochMilli
    val issuedAtTimestamp: Long,
    val expiresAtTimestamp: Long,
    val renewAtTimestamp: Long,
) {

    val needsToBeRenewed: Boolean
        get() = Instant.now().toEpochMilli() >= renewAtTimestamp

    @JvmInline
    @Serializable
    value class Key(val value: String) {

        fun hashed(): String =
            value.hashed()
    }

    @Serializable
    data class MessagesFromServer(
        val errors: List<String>,
        val warnings: List<String>,
        val info: List<String>,
    )

    @Serializable
    data class ConfigurationFromServer(
        val defaultConfiguration: Configuration,
        val enforcedConfiguration: Configuration,
    )

    @Serializable
    data class RequestData(
        val licenseKey: Key,
        val skieVersion: SkieVersion,
        val kotlinVersion: String,
        val isCI: Boolean,
        val hashedRootProjectDiskLocation: String,
        val hashedPlatformUUID: String,
        val hashedGitRemotes: List<String>,
    )
}
