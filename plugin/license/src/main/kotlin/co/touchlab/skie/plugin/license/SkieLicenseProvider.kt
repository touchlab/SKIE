package co.touchlab.skie.plugin.license

import co.touchlab.skie.util.directory.SkieDirectories
import java.nio.file.Path

object SkieLicenseProvider {

    // Gradle task config + doLast
    fun findLicenseLocationOrNull(requestData: SkieLicense.RequestData): Path? = TODO()

    fun loadLicense(skieDirectories: SkieDirectories): SkieLicense {
        val licensePath = skieDirectories.buildDirectory.license.toPath()
        TODO()


//        // Validate after analytics configuration so that we can log license errors
//        license.validate()

    }

    // Compiler, Gradle task config + doLast
    fun loadLicense(license: Path): SkieLicense {
        TODO()
    }

    // Gradle task doLast if getLicenseLocationOrNull returns null
    fun renewLicense(requestData: SkieLicense.RequestData): SkieLicense {
        TODO()
    }

    // Gradle task config (in background)
    fun tryToRenewLicenseIfExpiring(requestData: SkieLicense.RequestData) {
        val licenseLocation = findLicenseLocationOrNull(requestData)

        if (licenseLocation != null && loadLicense(licenseLocation).needsToBeRenewed) {
            tryToRenewLicense(requestData)
        }
    }

    // Gradle task doLast (in background) if getLicenseLocationOrNull does not return null
    fun tryToRenewLicense(requestData: SkieLicense.RequestData) {
        try {
            renewLicense(requestData)
        } catch (_: Throwable) {
        }
    }

//    private const val devLicenseKeyHash = "yhQJU775C2Ft9xGVAKmlBMZ0zIjZ3qffbZU3/JL7FS4="
//
//    fun getLicense(licenseKey: SkieLicense.Key): SkieLicense {
//        if (isDevLicense(licenseKey)) {
//            return SkieLicense(licenseKey, "SKIE Dev", SkieLicense.Environment.Dev)
//        }
//
//        return parseLicense(licenseKey)
//    }

//    private fun isDevLicense(licenseKey: SkieLicense.Key): Boolean =
//        licenseKey.hashed() == devLicenseKeyHash

//    private fun parseLicense(licenseKey: SkieLicense.Key): SkieLicense {
//        val claims = parseJwt(licenseKey)
//
//        check(claims["product"]?.asString() == "Skie") { "Touchlab license is valid but for a different product." }
//
//        val organizationKey = claims["apiKey"]?.asString() ?: error("SKIE License is malformatted.")
////        val licenseKey = claims["licenseKey"]?.asString() ?: error("SKIE License is malformatted.")
//        val environment = if (licenseKey.hashed() == devLicenseKeyHash) SkieLicense.Environment.Dev else SkieLicense.Environment.Production
//
//        return SkieLicense(licenseKey, licenseKey, environment)
//        TODO()
//    }


//
//    private fun createLicenseKeyfile(
//        apiKey: String,
//        licenseInfoDir: File = createLicenseInfoDir(),
//    ) = File(licenseInfoDir, "org-$apiKey")
//
//    private fun createLicenseInfoDir(): File {
//        val homeDir = File(System.getProperty("user.home"))
//        val licenseInfoDir = File(File(homeDir, ".touchlab"), "license")
//        licenseInfoDir.mkdirs()
//        return licenseInfoDir
//    }

// internal val FAKTORY_SERVER = "https://api.touchlab.dev"
//    @Serializable
//    data class TouchlabLicense(
//        val apiKey: String,
//        val licenseKey: String,
//        val product: String,
//        val expiration: Long,
//        val extraInfo: Map<String, String>
//    )
//    private val jwtDirectory = Path.of(System.getProperty("user.home")).resolve(".skie/license")
//
//    fun getLicense(): String =
//        getDevLicenseOrNull() ?: getProductionLicense()
//
//    private fun getDevLicenseOrNull(): String? =
//        project.extensions.extraProperties.properties.getOrDefault("touchlab.key.dev", null) as? String?
//
//    fun orgLicenseKeyFromProperties(properties: Map<String, Any?>): String {
//        val licenseKeyValue = properties[KEY_PROPERTY_NAME]
//            ?: throw SkieLicenseException("touchlab.key required in gradle properties")
//
//        return licenseKeyValue.toString()
//    }
//    private fun getProductionLicense(): String {
//        val orgLicense =
//            LicenseManager.orgLicenseKeyFromProperties(extensions.getByType(ExtraPropertiesExtension::class.java).properties)
//
//        return LicenseManager.findProductLicense(orgLicense, product)

//        val externallyParsedLicense = project.findProductLicense("Skie")
//
//        val licenseFile = getLicenseFile(externallyParsedLicense.apiKey)
//        val licensesInJson = licenseFile.readText()
//        val parsedLicenses = Json.decodeFromString<Licenses>(licensesInJson)
//
//        return findJwtWithLicenseByLicenseKey(parsedLicenses.licenses, externallyParsedLicense.licenseKey)
//            ?: error("Cannot find SKIE license in $licenseFile.")
//        TODO()
//    }

//    private fun getLicenseFile(apiKey: String): Path =
//        jwtDirectory.resolve("org-$apiKey")

//    val client = HttpClient(Java)
//    val result = runBlocking {
//        // TODO: Print errors if they happen
//        client.post("https://central.sonatype.com/v1/browse") {
//            accept(ContentType.Application.Json)
//            contentType(ContentType.Application.Json)
//            setBody("""
//                    {"size": 100, "page": $fromPage, "searchTerm": "$query", "filter": []}
//                """.trimIndent())
//        }.bodyAsText()
//    }
//
//
//    fun parseJwt(jwt: String): Map<String, Claim> {
//        val key = getPublicKey()
//
//        val algorithm = Algorithm.RSA256(key)
//
//        return  JWT.require(algorithm)
//            .withIssuer("admin@touchlab.co")
//            .build()
//            .verify(jwt)
//            .claims
//    }
//
//
//    private fun parseValidLicense(apiKey: String, jwt: String): TouchlabLicense? {
//        val claims: Jws<Claims> = try {
//            Jwts.parserBuilder()
//                .require("apiKey", apiKey)
//                .requireIssuer("admin@touchlab.co")
//                .setSigningKey(publicKey())
//                .build()
//                .parseClaimsJws(jwt)
//        } catch (e: Exception) {
//            BugsnagGlobal.report(e)
//            return null
//        }
//    }
//
//    fun parseJwtOrNull(jwt: String): Map<String, Claim>? =
//        try {
//            parseJwt(jwt)
//        } catch (_: Throwable) {
//            null
//        }
//
//    private fun getPublicKey(): RSAPublicKey {
//        val keyFactory = KeyFactory.getInstance("RSA")
//
//        val publicKeyBytes = Base64.getDecoder().decode(publicKeyPem)
//
//        val spec = X509EncodedKeySpec(publicKeyBytes)
//
//        return keyFactory.generatePublic(spec) as RSAPublicKey
//    }
//
//    private val publicKeyPem: String
//        get() = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAz5rJa1Q9kshv2fko5taO" +
//            "P/CNEeLWiKTi8zpaEt+7mXvMstsjO3dzVsAiR4TnKjRuXqcXK/DtQ1v5zOPyk8GK" +
//            "Qomo3ngkJsSy11jGNRnRT9hoBpZoJCQWlW4r1OWaR2CfgSb7W2lfYWyZ78Wtflzp" +
//            "o9tCFmvxtdjktlCzS5ikAk/xHikSvAWvNNVrAxf8AkDAyvSJUaHOVFq3yiiMeSA2" +
//            "aDhz1OW4b8IY3cnXeM1ElrPuGUgmXV11dOt0rcHvGs3yVJW1XJd8DH8lMaBV3jab" +
//            "ZTk0lJfm5CSHj9AWIDA9/3d019U7Gb9+xQLrC7jMgKtFG42r0KJfk5vGAYmwl5ZK" +
//            "BQIDAQAB"
}
