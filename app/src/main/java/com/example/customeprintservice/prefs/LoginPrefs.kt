package com.example.customeprintservice.prefs

import android.content.Context
import com.example.customeprintservice.utils.Constants.OCTA_TOKEN

class LoginPrefs {
    companion object {
        private val TOKEN = "token"

        fun saveOctaToken(context: Context, token: String) {
            val sharedPreferences = context.getSharedPreferences(OCTA_TOKEN, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(TOKEN, token).apply()
        }

        fun getOCTAToken(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(OCTA_TOKEN, Context.MODE_PRIVATE)
            return sharedPreferences.getString(TOKEN, null)
        }

        fun deleteToken(context: Context) {
            val sharedPreferences = context.getSharedPreferences(OCTA_TOKEN, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit().clear()
            editor.apply()
        }
    }
}