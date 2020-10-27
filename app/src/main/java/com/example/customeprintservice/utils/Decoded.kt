package com.example.customeprintservice.utils

import android.util.Base64
import android.util.Log
import java.io.UnsupportedEncodingException
import java.nio.charset.StandardCharsets

object Decoded {
    private lateinit var split: Array<String>
    @Throws(Exception::class)
    fun decoded(JWTEncoded: String): String {
        try {
            split = JWTEncoded.split("\\.".toRegex()).toTypedArray()
            Log.d("JWT_DECODED", "Header: " + getJson(split[0]))
            Log.d("JWT_DECODED", "Body: " + getJson(split[1]))
            Log.d("JWT_DECODED", "Signiture: " + getJson(split[2]))
        } catch (e: UnsupportedEncodingException) {
            //Error
        }
        return getJson(split[1])
    }

    @Throws(UnsupportedEncodingException::class)
    private fun getJson(strEncoded: String): String {
        val decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE)
        return String(decodedBytes, StandardCharsets.UTF_8)
    }
}