package com.example.customeprintservice.printjobstatus

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.printjobstatus.model.PrintJobStatusResponse
import com.example.customeprintservice.printjobstatus.model.jobstatus.JobStatusCancel
import com.example.customeprintservice.printjobstatus.model.jobstatus.JobStatusCanceledResponse
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import com.example.customeprintservice.utils.ProgressDialog
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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
                ProgressDialog.cancelLoading()
                val printJobStatusResponse = response.body().toString()
                Log.i("printer", "printJobStatusResponse=====>>${printJobStatusResponse}")

                Toast.makeText(context, "$printJobStatusResponse", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<PrintJobStatusResponse>, t: Throwable) {
                Log.i("printer", "Error=>${t.message}")
                ProgressDialog.cancelLoading()
                Toast.makeText(context, "${t.message}", Toast.LENGTH_SHORT).show()

            }
        })
    }

    fun printJobCancel(
        context: Context,
        jobStatusCancel: JobStatusCancel,
        userName: String,
        idpType: String,
        idpName: String
    ) {
        val BASE_URL = "https://gw.app.printercloud.com/devncookta/pq/api/job-statuses/cancel/"
        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call = apiService.jobStatusCancel(
            "Bearer "+LoginPrefs.getOCTAToken(context).toString(),
            userName, idpType, idpName,jobStatusCancel
        )

        call.enqueue(object : Callback<JobStatusCanceledResponse> {
            override fun onResponse(
                call: Call<JobStatusCanceledResponse>,
                response: Response<JobStatusCanceledResponse>
            ) {
                Log.i("printer","request body=>${Gson().toJson(call.request().body())}")
                Log.i("printer","request url=>${call.request().url()}")
                Log.i("printer", "printJobCancel result==>${response.body().toString()}")
            }

            override fun onFailure(call: Call<JobStatusCanceledResponse>, t: Throwable) {
                Log.i("printer", "printJobCancel Error==>${t.message}")
            }
        })
    }
}