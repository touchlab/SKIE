package co.touchlab.skie.plugin.license.util

import co.touchlab.skie.plugin.license.SkieLicense
import co.touchlab.skie.plugin.license.SkieLicenseError
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import java.nio.file.Path
import java.security.KeyFactory
import java.security.interfaces.RSAPublicKey
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import kotlin.io.path.readText

object JwtParser {

    private const val publicKeyPem: String =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAz5rJa1Q9kshv2fko5taO" +
            "P/CNEeLWiKTi8zpaEt+7mXvMstsjO3dzVsAiR4TnKjRuXqcXK/DtQ1v5zOPyk8GK" +
            "Qomo3ngkJsSy11jGNRnRT9hoBpZoJCQWlW4r1OWaR2CfgSb7W2lfYWyZ78Wtflzp" +
            "o9tCFmvxtdjktlCzS5ikAk/xHikSvAWvNNVrAxf8AkDAyvSJUaHOVFq3yiiMeSA2" +
            "aDhz1OW4b8IY3cnXeM1ElrPuGUgmXV11dOt0rcHvGs3yVJW1XJd8DH8lMaBV3jab" +
            "ZTk0lJfm5CSHj9AWIDA9/3d019U7Gb9+xQLrC7jMgKtFG42r0KJfk5vGAYmwl5ZK" +
            "BQIDAQAB"

    private val jwtVerifier = JWT.require(Algorithm.RSA256(getPublicKey())).build()

    private fun getPublicKey(): RSAPublicKey {
        val keyFactory = KeyFactory.getInstance("RSA")

        val publicKeyBytes = Base64.getDecoder().decode(publicKeyPem)

        val spec = X509EncodedKeySpec(publicKeyBytes)

        return keyFactory.generatePublic(spec) as RSAPublicKey
    }

    fun isValidJwt(path: Path): Boolean =
        tryParseJwt(path.readText()) != null

    fun isAnyJwt(path: Path): Boolean {
        try {
            JWT.decode(path.readText())

            return true
        } catch (_: Throwable) {
        }

        return false
    }

    fun tryParseJwtWithValidation(jwt: String, requestData: SkieLicense.RequestData): SkieLicense? =
        try {
            parseJwtWithValidation(jwt, requestData)
        } catch (_: SkieLicenseError) {
            null
        }

    private fun parseJwtWithValidation(jwt: String, requestData: SkieLicense.RequestData): SkieLicense {
        val license = parseJwt(jwt)

        license.validate(requestData)

        return license
    }

    fun tryParseJwt(jwt: String): SkieLicense? =
        try {
            parseJwt(jwt)
        } catch (_: SkieLicenseError) {
            null
        }

    fun parseJwt(jwt: String): SkieLicense {
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
}
