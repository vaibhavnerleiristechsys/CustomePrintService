package com.example.customeprintservice.prefs

import android.content.Context
import com.example.customeprintservice.utils.Constants.OCTA_TOKEN
import com.example.customeprintservice.utils.Constants.company_url
import com.example.customeprintservice.utils.Constants.site_Id

class LoginPrefs {
    companion object {
        private val TOKEN = "token"
        private val SITEID = "siteId"
        private val COMPANYURL = "companyUrl"

        fun saveOctaToken(context: Context, token: String) {
            val sharedPreferences = context.getSharedPreferences(OCTA_TOKEN, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(TOKEN, token).apply()
        }

        fun getOCTAToken(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(OCTA_TOKEN, Context.MODE_PRIVATE)
            return sharedPreferences.getString(TOKEN, null)
        }

        fun saveSiteId(context: Context, siteId: String) {
            val sharedPreferences = context.getSharedPreferences(site_Id, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(SITEID, siteId).apply()
        }

        fun getSiteId(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(site_Id, Context.MODE_PRIVATE)
            return sharedPreferences.getString(SITEID, null)
        }

        fun saveCompanyUrl(context: Context, companyUrl: String) {
            val sharedPreferences = context.getSharedPreferences(company_url, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(COMPANYURL, companyUrl).apply()
        }

        fun getCompanyUrl(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(company_url, Context.MODE_PRIVATE)
            return sharedPreferences.getString(COMPANYURL, null)
        }



        fun deleteToken(context: Context) {
            val sharedPreferences = context.getSharedPreferences(OCTA_TOKEN, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit().clear()
            editor.apply()
        }
    }
}