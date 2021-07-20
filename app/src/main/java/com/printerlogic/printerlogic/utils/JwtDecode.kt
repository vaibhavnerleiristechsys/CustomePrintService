package com.printerlogic.printerlogic.utils

import android.util.Base64
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets

object JwtDecode {

    private lateinit var split: Array<String>

    @Throws(Exception::class)
    fun decoded(JWTEncoded: String): String {
        try {
            split = JWTEncoded.split("\\.".toRegex()).toTypedArray()
//            Log.d("JWT_DECODED", "Body: " + getJson(split[1]))
        } catch (e: UnsupportedEncodingException) {
        }
        return getJson(split[1])
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getJson(strEncoded: String): String {
        val decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE)
        return String(decodedBytes, StandardCharsets.UTF_8)
    }
}