package com.example.ivlinereporting

import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey

object EncryptionUtils {
    private const val ALGORITHM = "AES"

    fun decrypt(encryptedData: String, key: SecretKey): String{
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key)
        val decodedBytes = Base64.getDecoder().decode(encryptedData)
        val decrypteedBytes = cipher.doFinal(decodedBytes)
        return String(decrypteedBytes)
    }
}