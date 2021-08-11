package com.printerlogic.printerlogic.printjobstatus

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log
import com.printerlogic.printerlogic.jipp.PrinterList
import com.printerlogic.printerlogic.jipp.PrinterModel
import com.printerlogic.printerlogic.prefs.LoginPrefs
import com.printerlogic.printerlogic.prefs.LoginPrefs.Companion.getTenantUrl
import com.printerlogic.printerlogic.print.PrintersFragment
import com.printerlogic.printerlogic.print.ServerPrintRelaseFragment
import com.printerlogic.printerlogic.printjobstatus.model.printerdetails.PrinterDetailsResponse
import com.printerlogic.printerlogic.printjobstatus.model.printerlist.PrinterListDesc
import com.printerlogic.printerlogic.rest.ApiService
import com.printerlogic.printerlogic.rest.RetrofitClient
import com.printerlogic.printerlogic.utils.ProgressDialog
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.InetAddress


class PrinterListService {
    var logger = LoggerFactory.getLogger(PrinterListService::class.java)
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
                    //Toast.makeText(context, "${response.body()}", Toast.LENGTH_LONG).show()
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
                //Toast.makeText(context, "${t.message}", Toast.LENGTH_SHORT).show()
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
                        //Toast.makeText(context, "HTML RESPONSE =>${html}", Toast.LENGTH_SHORT).show()
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
        @SuppressLint("WrongConstant")val sh: SharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_APPEND)
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")

        val tanentUrl = getTenantUrl(context)
        val BASE_URL = ""+tanentUrl+"/"+siteId+"/tree/api/node/"+nodeId+"/printer/"
      //  val BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/tree/api/node/"+nodeId+"/printer/"

        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)
        val call = if(IsLdap.equals("LDAP")){
            val sessionId = LoginPrefs.getSessionIdForLdap(context)
            apiService.getPrinterDetailsByNodeIdForLdap(
                siteId.toString(),
                LdapUsername.toString(),
                LdapPassword.toString(),
                "PHPSESSID=" + sessionId
            )
        }else if(idpType.toString().toLowerCase().equals("oidc")){
            logger.info("Devnco_Android API call: " + BASE_URL.toString() + " Token: " +authorization + " username: " + userName)
            apiService.getPrinterDetailsByNodeIdForGoogle(
                "Bearer "+authorization, userName, idpType, idpName,"serverId"
            )
        }
        else {
            logger.info("Devnco_Android API call: " + BASE_URL.toString() + " Token: " +authorization + " username: " + userName)
            apiService.getPrinterDetailsByNodeId(
                "Bearer "+authorization , userName, idpType, idpName
            )
        }
        call?.enqueue(object : Callback<PrinterDetailsResponse> {
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
                   //     val thread = Thread(Runnable {
                            try {
                                printerModel.printerHost =
                                    InetAddress.getByName(response.body()?.data?.attributes?.host_address.toString())
                            } catch (e: java.lang.Exception) {
                                e.printStackTrace()
                                Log.d("exception inetaddress :","exception in printer details inetaddress :"+e.message)
                            }
                     //   })

                       // thread.start()
                        // printerModel.printerHost =inetAddress
                        printerModel.printerPort = 631
                        printerModel.fromServer = true
                        printerModel.nodeId = nodeId
                        printerModel.location=response.body()?.data?.attributes?.location.toString()
                        printerModel.isColor=response.body()?.data?.attributes?.is_color
                        printerModel.id=response.body()?.data?.attributes?.id.toString()
                        printerModel.secure_release=response.body()?.data?.attributes?.secure_release.toString()
                        printerModel.isPullPrinter=response.body()?.data?.attributes?.is_pull_printer.toString()
                        PrinterList().addPrinterModel(printerModel)
                       // PrintersFragment().updateUi(PrinterList().printerList,context)
                        PrintersFragment.serverPrinterListWithDetails.add(printerModel)
                        if(response.body()?.data?.attributes?.is_pull_printer==1){
                            PrintersFragment.serverPullPrinterListWithDetails.add(printerModel)
                        }
                        if(response.body()?.data?.attributes?.is_pull_printer==0){
                            PrintersFragment.serverSecurePrinterListWithDetails.add(printerModel)
                            val prefs1 =
                                PreferenceManager.getDefaultSharedPreferences(context)
                            val gson1 = Gson()
                            val editor = prefs1.edit()
                            val json = gson1.toJson(PrintersFragment.serverSecurePrinterListWithDetails)
                            editor.putString("deployedsecurePrinterListWithDetails", json)
                            editor.apply()
                        }

                        val prefs1 =
                            PreferenceManager.getDefaultSharedPreferences(context)
                        val gson1 = Gson()
                        val editor = prefs1.edit()


                        PrintersFragment.deployedPrintersListForPrintPreivew.add(printerModel)
                        val json = gson1.toJson(PrintersFragment.deployedPrintersListForPrintPreivew)
                        editor.putString("deployedPrintersListForPrintPreivew", json)


                        val json1 = gson1.toJson(PrintersFragment.serverPrinterListWithDetails)
                        editor.putString("printerListWithDetails", json1)
                        editor.apply()
                    }
                }
                if(response.code()==429){
                  //  Toast.makeText(context, "Too Many Requests", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<PrinterDetailsResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                //Toast.makeText(context, "${t.message}", Toast.LENGTH_SHORT).show()
                Log.i("printer", "Error response==>${t.message.toString()}")
            }
        })
    }


}