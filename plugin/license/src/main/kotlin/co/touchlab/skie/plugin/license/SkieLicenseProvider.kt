package co.touchlab.skie.plugin.license

import co.touchlab.skie.util.hashed
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.impl.DefaultJwtParser
import io.jsonwebtoken.impl.DefaultJwtParserBuilder
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64

object SkieLicenseProvider {

    private const val devLicenseHash = "yhQJU775C2Ft9xGVAKmlBMZ0zIjZ3qffbZU3/JL7FS4="

    fun getLicense(jwt: String): SkieLicense {
        if (isDevLicense(jwt)) {
            return SkieLicense(jwt, jwt, SkieLicense.Environment.Dev)
        }

        return parseLicense(jwt)
    }

    private fun isDevLicense(jwt: String): Boolean =
        jwt.hashed() == devLicenseHash

    private fun parseLicense(jwt: String): SkieLicense {
        val claims = parseJwt(jwt)

        check(claims.body["product"] == "Skie") { "Passed license is valid but for a different product." }

        val organizationKey = claims.body["apiKey"] as String
        val licenseKey = claims.body["licenseKey"] as String
        val environment = if (licenseKey.hashed() == devLicenseHash) SkieLicense.Environment.Dev else SkieLicense.Environment.Production

        return SkieLicense(organizationKey, licenseKey, environment)
    }

    // TODO Using Jwts.parserBuilder() leads to ClassCastException in real projects
    fun parseJwt(jwt: String): Jws<Claims> =
        DefaultJwtParserBuilder()
            .setSigningKey(getPublicKey())
            .build()
            .parseClaimsJws(jwt)

    fun parseJwtOrNull(jwt: String): Jws<Claims>? =
        try {
            parseJwt(jwt)
        } catch (_: Throwable) {
            null
        }

    private fun getPublicKey(): PublicKey {
        val keyFactory = KeyFactory.getInstance("RSA")

        val publicKeyBytes = Base64.getDecoder().decode(publicKeyPem)

        val spec = X509EncodedKeySpec(publicKeyBytes)

        return keyFactory.generatePublic(spec)
    }

    private val publicKeyPem: String
        get() = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAz5rJa1Q9kshv2fko5taO" +
            "P/CNEeLWiKTi8zpaEt+7mXvMstsjO3dzVsAiR4TnKjRuXqcXK/DtQ1v5zOPyk8GK" +
            "Qomo3ngkJsSy11jGNRnRT9hoBpZoJCQWlW4r1OWaR2CfgSb7W2lfYWyZ78Wtflzp" +
            "o9tCFmvxtdjktlCzS5ikAk/xHikSvAWvNNVrAxf8AkDAyvSJUaHOVFq3yiiMeSA2" +
            "aDhz1OW4b8IY3cnXeM1ElrPuGUgmXV11dOt0rcHvGs3yVJW1XJd8DH8lMaBV3jab" +
            "ZTk0lJfm5CSHj9AWIDA9/3d019U7Gb9+xQLrC7jMgKtFG42r0KJfk5vGAYmwl5ZK" +
            "BQIDAQAB"
}
