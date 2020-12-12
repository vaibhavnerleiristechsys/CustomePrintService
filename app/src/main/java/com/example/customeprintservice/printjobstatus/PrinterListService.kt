package com.example.customeprintservice.printjobstatus

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.customeprintservice.printjobstatus.model.printerdetails.PrinterDetailsResponse
import com.example.customeprintservice.printjobstatus.model.printerlist.PrinterListDesc
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import com.example.customeprintservice.utils.ProgressDialog
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


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
        idpName: String
    ) {
        val BASE_URL =
            "https://gw.app.printercloud.com/devncookta/tree/api/node/4/printer/"

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
                    Log.i("printer", "lis res=>${response.body().toString()}")
                    Toast.makeText(context, "${response.body()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<PrinterDetailsResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Toast.makeText(context, "${t.message}", Toast.LENGTH_SHORT).show()
                Log.i("printer", "Error response==>${t.message.toString()}")
            }
        })
    }


    fun getPrinterList(
        context: Context
    ) {
        val BASE_URL =
            "https://devncookta.printercloud.com/client/gateway.php/"

        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call = apiService.getPrinterList(
            "devncookta",
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiIyMjMxN2RlMC05NDRkLTRhNjItOGIxNy03YjYyZWQ5OGM5Y2EiLCJpZHAiOiJPa3RhIiwic2l0ZSI6ImRldm5jb29rdGEiLCJ1c2VyIjoicmFuamVldGEuYmFsYWtyaXNobmFuQGRldm5jby5jbyIsInNlc3Npb24iOiJhOGI0NmE1Yi0wYWFlLTQ4ZjUtOWUxMS04NTM5YzljZDdkMTIiLCJleHAiOjE2MzkyMTQ0MzgsImlhdCI6MTYwNzY3ODQzOCwiaXNzIjoiY29tLnByaW50ZXJsb2dpYy5zZXJ2aWNlcy5hdXRobiIsImF1ZCI6ImNvbS5wcmludGVybG9naWMuY2xpZW50cy5kZXNrdG9wLmlkcCJ9.HKiyYRd0QNql6zRsz276L6nGgiQG0GHcYpA6s6h7dOZQoAJZI5G5nZfdPARUEX3vvnEqpy4E8xDrKepk24SoKOQB4dXoSfwg0B6D1B5sz7Dl8Pf6D0N0wvXQl9cEC2LNpv3WqI_qXPYXS6ihO926XSa6f7mo2j3pwmzPZkrO_Q8PSaAjNoXhfCgVXh4oDApTb8A-kO7D67ky9w-GjoMfLdieVqoD1DcWMKkGfFKIdAHDWsEuxamR7xvmtBVvtNnOKIEAxKwf_SqL2JDpMt4PEqvcGd1Cp2_WqREHpq5UG1t0go52PCY7YqCt9e6AypWE0KcxbOo9uoauXKIn5e95sA",
            "ranjeeta.balakrishnan@devnco.co",
            "saml2",
            "Okta",
            "1",
            "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                    "<system driverless=\"1\">\n" +
                    "  <machine>\n" +
                    "    <ips>\n" +
                    "      <ip mask=\"255.255.255.0\"> my-ip </ip>\n" +
                    "    </ips>\n" +
                    "  </machine>\n" +
                    "  <idp>\n" +
                    "    {\"idpName\": \"Okta\",\n" +
                    "      \"username\": \"ranjeeta.balakrishnan@devnco.co\",\n" +
                    "      \"isLoggedIn\": \"true\",\n" +
                    "      \"type\": \"auth-type\",\n" +
                    "      \"token\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiIyMjMxN2RlMC05NDRkLTRhNjItOGIxNy03YjYyZWQ5OGM5Y2EiLCJpZHAiOiJPa3RhIiwic2l0ZSI6ImRldm5jb29rdGEiLCJ1c2VyIjoicmFuamVldGEuYmFsYWtyaXNobmFuQGRldm5jby5jbyIsInNlc3Npb24iOiJhOGI0NmE1Yi0wYWFlLTQ4ZjUtOWUxMS04NTM5YzljZDdkMTIiLCJleHAiOjE2MzkyMTQ0MzgsImlhdCI6MTYwNzY3ODQzOCwiaXNzIjoiY29tLnByaW50ZXJsb2dpYy5zZXJ2aWNlcy5hdXRobiIsImF1ZCI6ImNvbS5wcmludGVybG9naWMuY2xpZW50cy5kZXNrdG9wLmlkcCJ9.HKiyYRd0QNql6zRsz276L6nGgiQG0GHcYpA6s6h7dOZQoAJZI5G5nZfdPARUEX3vvnEqpy4E8xDrKepk24SoKOQB4dXoSfwg0B6D1B5sz7Dl8Pf6D0N0wvXQl9cEC2LNpv3WqI_qXPYXS6ihO926XSa6f7mo2j3pwmzPZkrO_Q8PSaAjNoXhfCgVXh4oDApTb8A-kO7D67ky9w-GjoMfLdieVqoD1DcWMKkGfFKIdAHDWsEuxamR7xvmtBVvtNnOKIEAxKwf_SqL2JDpMt4PEqvcGd1Cp2_WqREHpq5UG1t0go52PCY7YqCt9e6AypWE0KcxbOo9uoauXKIn5e95sA\"}\n" +
                    "  </idp>\n" +
                    "  <memberships>\n" +
                    "    <computer />\n" +
                    "    <user>\n" +
                    "      <guid>S-1-1-0</guid>\n" +
                    "    </user>\n" +
                    "  </memberships>\n" +
                    "</system>"

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
                        val element = document.select("commands")

                        element.forEach {
                           Log.i("printer", "html res=>${it}")
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
}