package com.example.customeprintservice.printjobstatus

import android.content.Context
import android.util.Log
import com.example.customeprintservice.printjobstatus.model.printerlist.PrinterListDesc
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PrinterList {

    fun getPrinterNodeSession(
        context: Context,
        idpName: String,
        isLoggedIn: Boolean,
        userName: String,
        authType: String,
        token: String,
        isMobile: Boolean
    ) {
        val BASE_URL = "https://devncookta.printercloud.com/auth/asserted-login-portal/"

        val apiService = RetrofitClient(context)
            .getRetrofitInstanceXML(BASE_URL)
            .create(ApiService::class.java)

        val call = apiService.getPrinterList(
            idpName, isLoggedIn, userName, authType,
            token, isMobile
        )

        call.enqueue(object : Callback<PrinterListDesc> {
            override fun onResponse(
                call: Call<PrinterListDesc>,
                response: Response<PrinterListDesc>
            ) {
                if (response.isSuccessful) {
                    Log.i("printer", "lis res=>${response.body()}")
                }
            }

            override fun onFailure(call: Call<PrinterListDesc>, t: Throwable) {
                Log.i("printer", "Error response==>${t.message.toString()}")
            }
        })
    }

    fun getPrinterNodes(
        context: Context,
        cookei: String,
        searchRoot: String,
        search: String,
        mobile: String,
        adminMode: String,
        type: String,
        releaseStationConfId: String
    ) {
        val BASE_URL =
            "https://devncookta.printercloud.com/admin/query/ipaddressrange_printer_search.php/"

        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call = apiService.getPrinterNodes(
            cookei, searchRoot, search, mobile, adminMode, type, releaseStationConfId
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    try {
                        val html = response.body()?.string()
                        val document = Jsoup.parse(html)
                        val element = document.select("input[name=node_id]")
                        element.forEach {
                            Log.i("printer", "html res=>${it.attr("value")}")
                        }
                    } catch (e: Exception) {
                        Log.i("printer", "e=>${e.message.toString()}")
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.i("printer", "Error html response==>${t.message.toString()}")
            }
        })
    }

    fun getPrinterDetails(){


    }
}