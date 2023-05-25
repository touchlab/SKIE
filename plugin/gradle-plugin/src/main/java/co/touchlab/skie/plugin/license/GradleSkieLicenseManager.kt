package co.touchlab.skie.plugin.license

import co.touchlab.skie.configuration.Configuration
import co.touchlab.skie.util.Environment
import org.gradle.api.Project
import org.gradle.api.provider.Property

internal class GradleSkieLicenseManager(private val project: Project) {

    val licenseOrNull: SkieLicense?
        get() = license.orNull

    val license: Property<SkieLicense> = project.objects.property(SkieLicense::class.java)

    fun configureLicensing() {
        license.set(
            SkieLicense(
                licenseKey = SkieLicense.Key(""),
                organizationId = "",
                licensedSkieVersion = SkieVersion(""),
                supportedKotlinVersions = SerializableRegex(""),
                licensedHashedRootProjectDiskLocations = SerializableRegex(""),
                licensedHashedPlatformUUID = "",
                environment = Environment.Dev,
                configurationFromServer = SkieLicense.ConfigurationFromServer(Configuration(), Configuration()),
                serverMessagesForGradleConfigPhase = SkieLicense.MessagesFromServer(listOf(), listOf(), listOf()),
                serverMessagesForCompiler = SkieLicense.MessagesFromServer(listOf(), listOf(), listOf()),
                issuedAtTimestamp = 0L,
                expiresAtTimestamp = 0L,
                renewAtTimestamp = 0L,
            ),
        )
    }
}
