package com.example.customeprintservice.prefs

import android.content.Context
import com.example.customeprintservice.utils.Constants.ClientSecret
import com.example.customeprintservice.utils.Constants.OCTA_TOKEN
import com.example.customeprintservice.utils.Constants.TenantUrl
import com.example.customeprintservice.utils.Constants.client_id
import com.example.customeprintservice.utils.Constants.company_url
import com.example.customeprintservice.utils.Constants.googleToken_url
import com.example.customeprintservice.utils.Constants.site_Id

class LoginPrefs {
    companion object {
        private val TOKEN = "token"
        private val SITEID = "siteId"
        private val COMPANYURL = "companyUrl"
        private val GOOGLETOKENURL = "googleTokenUrl"
        private val clientID = "clientId"
        private val TenantURL="TenantURL"
        private val ClientSecret= "ClientSecret"

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



        fun savegoogleTokenUrl(context: Context, googleTokenUrl: String) {
            val sharedPreferences = context.getSharedPreferences(googleToken_url, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(GOOGLETOKENURL, googleTokenUrl).apply()
        }

        fun getgoogleTokenUrl(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(googleToken_url, Context.MODE_PRIVATE)
            return sharedPreferences.getString(GOOGLETOKENURL, null)
        }

        fun saveClientId(context: Context, clientId: String) {
            val sharedPreferences = context.getSharedPreferences(client_id, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(clientID, clientId).apply()
        }

        fun getClientId(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(client_id, Context.MODE_PRIVATE)
            return sharedPreferences.getString(clientID, null)
        }

        fun saveClientSecret(context: Context, clientSecret: String) {
            val sharedPreferences = context.getSharedPreferences(ClientSecret, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(ClientSecret, clientSecret).apply()
        }

        fun getClientSecret(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(ClientSecret, Context.MODE_PRIVATE)
            return sharedPreferences.getString(ClientSecret, null)
        }



        fun saveTenantUrl(context: Context, clientSecret: String) {
            val sharedPreferences = context.getSharedPreferences(TenantUrl, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(TenantURL, clientSecret).apply()
        }

        fun getTenantUrl(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(TenantUrl, Context.MODE_PRIVATE)
            return sharedPreferences.getString(TenantURL, null)
        }


        fun deleteToken(context: Context) {
            val sharedPreferences = context.getSharedPreferences(OCTA_TOKEN, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit().clear()
            editor.apply()
        }
    }
}