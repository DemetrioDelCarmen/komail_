package mx.com.david.komali.utility

import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class Encriptar {
    @Throws(NoSuchAlgorithmException::class)
    fun getHA(password: String): ByteArray? {
        val messageDigest =
            MessageDigest.getInstance("SHA-256")
        return messageDigest.digest(password.toByteArray(StandardCharsets.UTF_8))
    }

    fun toMexString(hash: ByteArray?): String? {
        val number = BigInteger(1, hash)
        val hexString = StringBuilder(number.toString(16))
        while (hexString.length < 32) {
            hexString.insert(0, '0')
        }
        return hexString.toString()
    }
}