package com.example.customeprintservice.prefs

import android.content.Context
import com.example.customeprintservice.utils.Constants.ClientSecret
import com.example.customeprintservice.utils.Constants.Guids
import com.example.customeprintservice.utils.Constants.OCTA_TOKEN
import com.example.customeprintservice.utils.Constants.StartJob
import com.example.customeprintservice.utils.Constants.TenantUrl
import com.example.customeprintservice.utils.Constants.UpdateGuids
import com.example.customeprintservice.utils.Constants.client_id
import com.example.customeprintservice.utils.Constants.company_url
import com.example.customeprintservice.utils.Constants.googleToken_url
import com.example.customeprintservice.utils.Constants.jobIds
import com.example.customeprintservice.utils.Constants.site_Id
import com.example.customeprintservice.utils.Constants.workStation

class LoginPrefs {
    companion object {
        private val TOKEN = "token"
        private val SITEID = "siteId"
        private val COMPANYURL = "companyUrl"
        private val GOOGLETOKENURL = "googleTokenUrl"
        private val clientID = "clientId"
        private val TenantURL="TenantURL"
        private val ClientSecret= "ClientSecret"
        private val WORKSTATIONID ="workStationId"
        private val JOBID ="JOBID"
        private val GUID ="GUID"
        private val UPDATEGUID="UPDATEGUID"
        private val STARTJOB = "STARTJOB"

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


        fun saveworkSatationId(context: Context, workStationId: String) {
            val sharedPreferences = context.getSharedPreferences(workStation, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(WORKSTATIONID, workStationId).apply()
        }

        fun getworkSatationId(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(workStation, Context.MODE_PRIVATE)
            return sharedPreferences.getString(WORKSTATIONID, null)
        }

        fun saveJobId(context: Context, jobId: String) {
            val sharedPreferences = context.getSharedPreferences(jobIds, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(JOBID, jobId).apply()
        }

        fun getLastJobId(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(jobIds, Context.MODE_PRIVATE)
            return sharedPreferences.getString(JOBID, null)
        }

        fun saveGuId(context: Context, GuId: String) {
            val sharedPreferences = context.getSharedPreferences(Guids, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(GUID, GuId).apply()
        }

        fun getGuId(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(Guids, Context.MODE_PRIVATE)
            return sharedPreferences.getString(GUID, null)
        }

        fun saveGuIdForUpdate(context: Context, updateGuId: String) {
            val sharedPreferences = context.getSharedPreferences(UpdateGuids, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(UPDATEGUID, updateGuId).apply()
        }



        fun getGuIdForUpdate(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(UpdateGuids, Context.MODE_PRIVATE)
            return sharedPreferences.getString(UPDATEGUID, null)
        }



        fun setStartJobIdMethod(context: Context) {
            val sharedPreferences = context.getSharedPreferences(StartJob, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(STARTJOB, "Started").apply()
        }

        fun getStartJobIdMethod(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(StartJob, Context.MODE_PRIVATE)
            return sharedPreferences.getString(STARTJOB, null)
        }


        fun deleteToken(context: Context) {
            val sharedPreferences = context.getSharedPreferences(OCTA_TOKEN, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit().clear()
            editor.apply()
        }

    }
}