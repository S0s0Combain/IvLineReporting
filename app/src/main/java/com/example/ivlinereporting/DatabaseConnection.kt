package com.example.ivlinereporting

import java.sql.Connection
import java.sql.DriverManager
import java.util.Base64
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

class DatabaseConnection {
    private val encryptedUrl = "gWs1xMuFmj945xd5vQOeuhPt1cqu5Pfw6HgoadsWCBgrhR56Zt+R/rVxpKkFVPv6Pz9EQCYsy/u4uaLcrqUF4A=="
    private val encryptedUser = "XDjUJcZv6lVynNgmNwkNlw=="
    private val encryptedPass = "5wKVUHLdMPn2NOpfwinFVA=="
    private val keyString = "Oe5HbGlus6zQ2joeJZvjvg=="

    private val key: SecretKey
        get(){
            val decodedKey = Base64.getDecoder().decode(keyString)
            return SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
        }

    fun createConnection():Connection{
        val url = EncryptionUtils.decrypt(encryptedUrl, key)
        val user = EncryptionUtils.decrypt(encryptedUser, key)
        val pass = EncryptionUtils.decrypt(encryptedPass, key)
        return DriverManager.getConnection(url, user, pass)
    }

    fun closeConnection(connection: Connection){
        connection.close()
    }
}