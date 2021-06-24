package com.example.customeprintservice.prefs

import android.content.Context
import com.example.customeprintservice.signin.GoogleLoginActivity
import com.example.customeprintservice.utils.Constants

class SignInCompanyPrefs {


    companion object {

        fun saveIdpUrl(context: Context, idpUrl: String) {
            val sharedPreferences =
                context.getSharedPreferences(Constants.IDP_URL, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(Constants.IDP_URL, idpUrl).apply()
        }

        fun getIdpUrl(context: Context): String? {
            val sharedPreferences =
                context.getSharedPreferences(Constants.IDP_URL, Context.MODE_PRIVATE)
            return sharedPreferences.getString(Constants.IDP_URL, null)
           // return "AzureAD"
        }

        fun saveIdpType(context: Context, idpType: String) {
            val sharedPreferences =
                context.getSharedPreferences(Constants.IDP_TYPE, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(Constants.IDP_TYPE, idpType).apply()
        }

        fun getIdpType(context: Context): String? {
            val sharedPreferences =
                context.getSharedPreferences(Constants.IDP_TYPE, Context.MODE_PRIVATE)
            return sharedPreferences.getString(Constants.IDP_TYPE, null)
           //return "saml2"

        }

        fun saveIdpName(context: Context, idpType: String) {
            val sharedPreferences =
                context.getSharedPreferences(Constants.IDP_NAME, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(Constants.IDP_NAME, idpType).apply()
        }

        fun getIdpName(context: Context): String? {
            val sharedPreferences =
                context.getSharedPreferences(Constants.IDP_NAME, Context.MODE_PRIVATE)
            return sharedPreferences.getString(Constants.IDP_NAME, null)
            //return "AzureAD"
        }


    }
}