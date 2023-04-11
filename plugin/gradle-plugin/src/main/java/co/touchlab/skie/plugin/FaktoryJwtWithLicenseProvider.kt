package co.touchlab.skie.plugin

import co.touchlab.faktory.access.findProductLicense
import co.touchlab.skie.plugin.license.SkieLicenseProvider
import org.gradle.api.Project
import java.nio.file.Path
import kotlin.io.path.readText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

class FaktoryJwtWithLicenseProvider(private val project: Project) {

    private val jwtDirectory = Path.of(System.getProperty("user.home")).resolve(".touchlab/license")

    fun getJwtWithLicense(): String =
        getDevLicenseOrNull() ?: getProductionLicense()

    private fun getDevLicenseOrNull(): String? =
        project.extensions.extraProperties.properties.getOrDefault("touchlab.key.dev", null) as? String?

    private fun getProductionLicense(): String {
        val externallyParsedLicense = project.findProductLicense("Skie")

        val licenseFile = getLicenseFile(externallyParsedLicense.apiKey)
        val licensesInJson = licenseFile.readText()
        val parsedLicenses = Json.decodeFromString<Licenses>(licensesInJson)

        return findJwtWithLicenseByLicenseKey(parsedLicenses.licenses, externallyParsedLicense.licenseKey)
            ?: error("Cannot find SKIE license in $licenseFile.")
    }

    private fun getLicenseFile(apiKey: String): Path =
        jwtDirectory.resolve("org-$apiKey")

    @Serializable
    private data class Licenses(val licenses: List<String>)

    private fun findJwtWithLicenseByLicenseKey(jwts: List<String>, licenseKey: String): String? =
        jwts.map { it to SkieLicenseProvider.parseJwtOrNull(it) }
            .firstOrNull { it.second?.body?.get("licenseKey") == licenseKey }
            ?.first
}
