package co.touchlab.skie.plugin.analytics.producer

import java.nio.ByteBuffer
import java.nio.file.Path
import java.security.Key
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.spec.IvParameterSpec
import kotlin.io.path.readBytes

object AnalyticsEncryptor {

    private const val asymmetricCipher = "RSA"
    private const val symmetricCipher = "AES"
    private const val symmetricCipherWithMode = "$symmetricCipher/CBC/PKCS5Padding"

    private const val aesKeySize = 256

    private const val publicKeyString =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAw2Rdkblx90bq/8ivrQ6N99G5z7R2o1CKswCc5bsjljLV3DX2xM379RZ5sYrwosyDHHYujUOM21jHQDKraWTbBFm67unX6ATkg94nDxnKiIvBozxch91TjPxzqFcUnbusRK5VQQMpnPX2dH/CSL3wjYg+Pos7UhhlXy3ePQpHBoYsdsGH4hlCCeH787TlQYrAbbe2Ay6+T9idtW+Xyyw1CunwSIRN54HscBuav9OkI0RpDftX0InmHjqDN90aRULFgJNIbtpDxbRcippi7nYIkaXSyOIBjIkYh3ea/Hvw4RafKoIwpTr4MohE2Rp6s3jyw7ROT5+RbgTG1xan/R2fJQIDAQAB"

    private val publicKey: PublicKey
        get() {
            val byteKey = Base64.getDecoder().decode(publicKeyString)
            val keySpec = X509EncodedKeySpec(byteKey)
            val keyFactory = KeyFactory.getInstance(asymmetricCipher)
            return keyFactory.generatePublic(keySpec)
        }

    fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(symmetricCipherWithMode)

        val symmetricKey = generateSymmetricCipherKey()

        cipher.init(Cipher.ENCRYPT_MODE, symmetricKey, cipher.zeroIv())

        val encryptedKey = encryptKey(symmetricKey)
        val encryptedData = cipher.doFinal(data)

        return DataWithHeader(encryptedKey, encryptedData).toByteArrayWithHeader()
    }

    private fun generateSymmetricCipherKey(): Key {
        val keyGen = KeyGenerator.getInstance(symmetricCipher)

        keyGen.init(aesKeySize)

        return keyGen.generateKey()
    }

    private fun encryptKey(key: Key): ByteArray {
        val cipher = Cipher.getInstance(asymmetricCipher)

        cipher.init(Cipher.WRAP_MODE, publicKey)

        return cipher.wrap(key)
    }

    fun decrypt(byteArrayWithHeader: ByteArray, privateKeyPath: Path): ByteArray {
        val privateKey = loadPrivateKey(privateKeyPath)

        val dataWithHeader = DataWithHeader.parse(byteArrayWithHeader)

        val symmetricKey = decryptKey(dataWithHeader.key, privateKey)

        val cipher = Cipher.getInstance(symmetricCipherWithMode)

        cipher.init(Cipher.DECRYPT_MODE, symmetricKey, cipher.zeroIv())

        return cipher.doFinal(dataWithHeader.data)
    }

    private fun loadPrivateKey(path: Path): PrivateKey {
        val keyBytes = path.readBytes()
        val keySpec = PKCS8EncodedKeySpec(keyBytes)
        val keyFactory = KeyFactory.getInstance(asymmetricCipher)
        return keyFactory.generatePrivate(keySpec)
    }

    private fun decryptKey(encryptedKey: ByteArray, privateKey: PrivateKey): Key {
        val cipher = Cipher.getInstance(asymmetricCipher)

        cipher.init(Cipher.UNWRAP_MODE, privateKey)

        return cipher.unwrap(encryptedKey, symmetricCipher, Cipher.SECRET_KEY)
    }

    fun test(text: String, privateKeyPath: Path) {
        val encrypted = encrypt(text.toByteArray())

        val decrypted = decrypt(encrypted, privateKeyPath)

        assert(String(decrypted) == text)
    }

    private class DataWithHeader(val key: ByteArray, val data: ByteArray) {

        // key_size: Int, key: ByteArray, data: ByteArray
        fun toByteArrayWithHeader(): ByteArray {
            val buffer = ByteBuffer.allocate(Int.SIZE_BYTES + key.size + data.size)

            buffer.putInt(key.size)
            buffer.put(key)
            buffer.put(data)

            return buffer.array()
        }

        companion object {

            fun parse(byteArrayWithHeader: ByteArray): DataWithHeader {
                val buffer = ByteBuffer.wrap(byteArrayWithHeader)

                val keySize = buffer.int
                val key = ByteArray(keySize).also { buffer.get(it) }
                val data = ByteArray(byteArrayWithHeader.size - keySize - Int.SIZE_BYTES).also { buffer.get(it) }

                return DataWithHeader(key, data)
            }
        }
    }
}

private fun Cipher.zeroIv(): IvParameterSpec =
    IvParameterSpec(ByteArray(blockSize))
