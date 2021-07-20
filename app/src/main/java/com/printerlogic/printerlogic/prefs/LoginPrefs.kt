package com.printerlogic.printerlogic.prefs

import android.content.Context
import com.printerlogic.printerlogic.utils.Constants.Guids
import com.printerlogic.printerlogic.utils.Constants.OCTA_TOKEN
import com.printerlogic.printerlogic.utils.Constants.SessionId
import com.printerlogic.printerlogic.utils.Constants.StartJob
import com.printerlogic.printerlogic.utils.Constants.TenantUrl
import com.printerlogic.printerlogic.utils.Constants.UpdateGuids
import com.printerlogic.printerlogic.utils.Constants.client_id
import com.printerlogic.printerlogic.utils.Constants.company_url
import com.printerlogic.printerlogic.utils.Constants.googleToken_url
import com.printerlogic.printerlogic.utils.Constants.jobIds
import com.printerlogic.printerlogic.utils.Constants.site_Id
import com.printerlogic.printerlogic.utils.Constants.workStation

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
        private val SESSIONID="SESSIONID"
        private val STARTJOB = "STARTJOB"

        fun saveOctaToken(context: Context, token: String) {
            val sharedPreferences = context.getSharedPreferences(OCTA_TOKEN, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(TOKEN, token).apply()
        }

        fun getOCTAToken(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(OCTA_TOKEN, Context.MODE_PRIVATE)
            return sharedPreferences.getString(TOKEN, null)
           // return "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiIxMmI4MDJmOS1mMzUwLTQ5YmUtYWFmNi01NWQxZmE3MzQ2ZmQiLCJpZHAiOiJBenVyZUFEIiwic2l0ZSI6Im1pa2VtYXJzaGFsbC1kZW1vIiwidXNlciI6Im1pa2VAbWlrZW1hcnNoYWxsLm1lIiwic2Vzc2lvbiI6ImI3Mjg4OTFlLTY3YmUtNDE4Ny05N2M0LTg2ZTMzYzQwNmIxNiIsImV4cCI6MTY1NTkwOTczNSwiaWF0IjoxNjI0MzczNzM1LCJpc3MiOiJjb20ucHJpbnRlcmxvZ2ljLnNlcnZpY2VzLmF1dGhuIiwiYXVkIjoiY29tLnByaW50ZXJsb2dpYy5jbGllbnRzLmRlc2t0b3AuaWRwIn0.g6HX2dXwRRkGfnDInw1rqSazH0qt-HyiNrCcaa24Lhc3NP-_Dkj9_YXFrSyDBCsjvYvSBCPJfTh2RYhssGrj8KKM8d-3Llg_ZsECXmq6XuMH74e1MoVInqb_g2K9qUdnZoo2lcfCF-O1Vk4okCE3KLRM8Rqd0tO8UAKnzvwxDx1hUM2Ak9LVFC6feoKcOQmvk4c3OmkMYlkd_VaQQ1852eUjxWMOAW4tGG2WLWEwlXcQeFbs_8SkSrUjaHPCQ5thtm1kX8rzapjhFOY-nnz9ngTWTYGMlWxsDaZQ5pPNzSQHpf3whYqevO2gxB6BUo2q42IWYNtuBqGlaoqzi9_-Ug"
        }

        fun saveSiteId(context: Context, siteId: String) {
            val sharedPreferences = context.getSharedPreferences(site_Id, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(SITEID, siteId).apply()
        }

        fun getSiteId(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(site_Id, Context.MODE_PRIVATE)
            return sharedPreferences.getString(SITEID, null)
          //  return "mikemarshall-demo"
        }

        fun saveCompanyUrl(context: Context, companyUrl: String) {
            val sharedPreferences = context.getSharedPreferences(company_url, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(COMPANYURL, companyUrl).apply()
        }

        fun getCompanyUrl(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(company_url, Context.MODE_PRIVATE)
            return sharedPreferences.getString(COMPANYURL, null)
           // return "mikemarshall-demo.printercloud.com"
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


        fun saveSessionIdForLdap(context: Context, sessionId: String) {
            val sharedPreferences = context.getSharedPreferences(SessionId, Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            editor.putString(SESSIONID, sessionId).apply()
        }



        fun getSessionIdForLdap(context: Context): String? {
            val sharedPreferences = context.getSharedPreferences(SessionId, Context.MODE_PRIVATE)
            return sharedPreferences.getString(SESSIONID, null)
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