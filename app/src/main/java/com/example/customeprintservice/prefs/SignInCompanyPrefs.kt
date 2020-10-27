package com.example.customeprintservice.prefs

import android.content.Context
import com.example.customeprintservice.utils.Constants

class SignInCompanyPrefs {
    companion object {
        private val IdpUrl = "idpurl"

        fun saveIdpUrl(context: Context, idpUrl: String) {
            val sharedPreferences = context.getSharedPreferences(Constants.IDP_URL, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(IdpUrl, idpUrl).apply()
        }

        fun getIdpUrl(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(Constants.IDP_URL, Context.MODE_PRIVATE)
            return sharedPreferences.getString(IdpUrl, null)
        }
    }
}