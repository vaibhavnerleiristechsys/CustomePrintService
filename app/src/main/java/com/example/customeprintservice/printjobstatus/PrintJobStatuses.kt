package com.example.customeprintservice.printjobstatus

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import com.example.customeprintservice.utils.ProgressDialog
import okhttp3.ResponseBody
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
        val siteId= LoginPrefs.getSiteId(context)
        val BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/pq/api/job-statuses/"
        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call =
            apiService.printJobstatus(
                "Bearer " + LoginPrefs.getOCTAToken(context).toString(), userName, idpType, idpName
            )

        call?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                ProgressDialog.cancelLoading()
//                val printJobStatusResponse = response.body().toString()
                Log.i("printer", "printJobStatusResponse=====>>${response.body().toString()}")

                Toast.makeText(context, "$response.body().toString()", Toast.LENGTH_LONG).show()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.i("printer", "Error=>${t.message}")
                ProgressDialog.cancelLoading()
                Toast.makeText(context, "${t.message}", Toast.LENGTH_SHORT).show()

            }
        })
    }
//
//    fun printJobCancel(
//        context: Context,
//        jobStatusCancel: JobStatusCancel,
//        userName: String,
//        idpType: String,
//        idpName: String
//    ) {
//        val BASE_URL = "https://gw.app.printercloud.com/devncookta/pq/api/job-statuses/cancel/"
//        val apiService = RetrofitClient(context)
//            .getRetrofitInstance(BASE_URL)
//            .create(ApiService::class.java)
//
//        val call = apiService.jobStatusCancel(
//            "Bearer " + LoginPrefs.getOCTAToken(context).toString(),
//            userName, idpType, idpName, jobStatusCancel
//        )
//
//        call.enqueue(object : Callback<JobStatusCanceledResponse> {
//            override fun onResponse(
//                call: Call<JobStatusCanceledResponse>,
//                response: Response<JobStatusCanceledResponse>
//            ) {
//                Log.i("printer", "request body=>${Gson().toJson(call.request().body())}")
//                Log.i("printer", "request url=>${call.request().url()}")
//                Log.i("printer", "printJobCancel result==>${response.body().toString()}")
//            }
//
//            override fun onFailure(call: Call<JobStatusCanceledResponse>, t: Throwable) {
//                Log.i("printer", "printJobCancel Error==>${t.message}")
//            }
//        })
//    }

//
//    fun getJobStatuses(context: Context, userName: String, idpType: String, idpName: String) {
//        val BASE_URL = "https://gw.app.printercloud.com/devncookta/pq/api/job-statuses/"
//        val apiService = RetrofitClient(context)
//            .getRetrofitInstance(BASE_URL)
//            .create(ApiService::class.java)
//
//        val call = apiService.getPrintJobStatuses(
//            "Bearer " + LoginPrefs.getOCTAToken(context),
//            userName,
//            idpType,
//            idpName
//        )
//        call.enqueue(object : Callback<GetJobStatusesResponse> {
//            override fun onResponse(
//                call: Call<GetJobStatusesResponse>,
//                response: Response<GetJobStatusesResponse>
//            ) {
//                val getJobStatusesResponse = response.body().toString()
//
//
//            }
//
//            override fun onFailure(call: Call<GetJobStatusesResponse>, t: Throwable) {
//                Log.i("printer", t.message.toString())
//            }
//        })
//    }
}