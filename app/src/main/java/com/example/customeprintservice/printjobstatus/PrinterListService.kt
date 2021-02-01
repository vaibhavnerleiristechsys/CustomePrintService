package com.example.customeprintservice.printjobstatus

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import android.widget.Toast
import com.example.customeprintservice.jipp.PrinterModel
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.print.PrintersFragment
import com.example.customeprintservice.print.ServerPrintRelaseFragment
import com.example.customeprintservice.printjobstatus.model.printerdetails.PrinterDetailsResponse
import com.example.customeprintservice.printjobstatus.model.printerlist.PrinterListDesc
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import com.example.customeprintservice.utils.ProgressDialog
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.InetAddress


class PrinterListService {

    fun getPrinterNodeSession(
        context: Context,
        idpName: String,
        isLoggedIn: Boolean,
        userName: String,
        authType: String,
        token: String,
        isMobile: Boolean
    ) {
        val companyUrl =LoginPrefs.getCompanyUrl(context)
        val BASE_URL = "https://"+companyUrl+"/auth/asserted-login-portal/"

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
                ProgressDialog.cancelLoading()
                if (response.isSuccessful) {
                    Log.i("printer", "lis res=>${response.body()?.desc.toString()}")
                    Toast.makeText(context, "${response.body()}", Toast.LENGTH_LONG).show()
                    getPrinterNodes(
                        context,
                        "PHPSESSID=" + response.body()?.desc.toString(),
                        "0",
                        "",
                        "0",
                        "0",
                        "pull-release-printer",
                        "-1"
                    )

                }
            }

            override fun onFailure(call: Call<PrinterListDesc>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Toast.makeText(context, "${t.message}", Toast.LENGTH_SHORT).show()
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
        val companyUrl =LoginPrefs.getCompanyUrl(context)
        val BASE_URL =
            "https://"+companyUrl+"/admin/query/ipaddressrange_printer_search.php/"

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
                ProgressDialog.cancelLoading()
                if (response.isSuccessful) {
                    try {
                        val html = response.body()?.string()
                        Toast.makeText(context, "HTML RESPONSE =>${html}", Toast.LENGTH_SHORT)
                            .show()
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
                ProgressDialog.cancelLoading()
                Log.i("printer", "Error html response==>${t.message.toString()}")
            }
        })
    }

    fun getPrinterDetails(
        context: Context,
        authorization: String,
        userName: String,
        idpType: String,
        idpName: String,
        nodeId: String,
        getprintersdetails:Boolean
    ) {
        val siteId= LoginPrefs.getSiteId(context)
        val BASE_URL =
            "https://gw.app.printercloud.com/"+siteId+"/tree/api/node/"+nodeId+"/printer/"

        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call = apiService.getPrinterDetailsByNodeId(
            authorization, userName, idpType, idpName
        )

        call.enqueue(object : Callback<PrinterDetailsResponse> {
            override fun onResponse(
                call: Call<PrinterDetailsResponse>,
                response: Response<PrinterDetailsResponse>
            ) {
                ProgressDialog.cancelLoading()
                if (response.isSuccessful) {
                    Log.i("printer", "lis res=>${response.body()?.data?.attributes.toString()}")


                    Log.i("printer id", "lis res=>${response.body()?.data?.type.toString()}")
                    var finalLocalurl = "http" + "://" + response.body()?.data?.attributes?.host_address.toString() + ":" +response.body()?.data?.attributes?.port_number.toString()  + "/ipp/print"

                    finalLocalurl = finalLocalurl.replace("///", "//")
                    ServerPrintRelaseFragment.localPrinturl=finalLocalurl
                    ServerPrintRelaseFragment.secure_release= response.body()?.data?.attributes?.secure_release!!
                    // ServerPrintRelaseFragment.serverDocumentlist.add(selectedFile);
                 //   Toast.makeText(context, "${response.body()}", Toast.LENGTH_LONG).show()
                    if(getprintersdetails==true) {
                        val printerModel: PrinterModel = PrinterModel()
                        printerModel.serviceName =
                            response.body()?.data?.attributes?.title.toString()
                        printerModel.printerHost =
                            InetAddress.getByName(response.body()?.data?.attributes?.host_address.toString())
                        // printerModel.printerHost =inetAddress
                        printerModel.printerPort = 631
                        printerModel.fromServer = true
                        printerModel.nodeId = nodeId
                        PrintersFragment.serverPrinterListWithDetails.add(printerModel)
                        if(response.body()?.data?.attributes?.is_pull_printer==1){
                            PrintersFragment.serverPullPrinterListWithDetails.add(printerModel)
                        }



                        val prefs1 =
                            PreferenceManager.getDefaultSharedPreferences(context)
                        val gson1 = Gson()
                        val editor = prefs1.edit()
                        val json1 = gson1.toJson(PrintersFragment.serverPrinterListWithDetails)
                        editor.putString("printerListWithDetails", json1)
                        editor.apply()
                    }
                }
            }

            override fun onFailure(call: Call<PrinterDetailsResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Toast.makeText(context, "${t.message}", Toast.LENGTH_SHORT).show()
                Log.i("printer", "Error response==>${t.message.toString()}")
            }
        })
    }


}