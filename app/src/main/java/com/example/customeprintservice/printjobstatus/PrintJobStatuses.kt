package com.example.customeprintservice.printjobstatus

import android.content.Context
import android.util.Log
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.printjobstatus.model.PrintJobStatusResponse
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PrintJobStatuses {

    fun getPrintJobStatuses(
        context: Context,
        userName: String,
        idpType: String,
        idpName: String
    ) {
        val BASE_URL = "https://gw.app.printercloud.com/devncookta/pq/api/job-statuses/"
        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call =
            apiService.printJobstatus(LoginPrefs.getOCTAToken(context), userName, idpType, idpName)

        call?.enqueue(object : Callback<PrintJobStatusResponse> {
            override fun onResponse(
                call: Call<PrintJobStatusResponse>,
                response: Response<PrintJobStatusResponse>
            ) {
                val printJobStatusResponse = response.body().toString()
                Log.i("printer", "printJobStatusResponse=====>>${printJobStatusResponse}")
            }

            override fun onFailure(call: Call<PrintJobStatusResponse>, t: Throwable) {
                Log.i("printer", "Error=>${t.message}")
            }
        })
    }
}