package com.example.customeprintservice.print

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.R
import com.example.customeprintservice.adapter.FragmentPrinterListAdapter
import com.example.customeprintservice.jipp.PrinterList
import com.example.customeprintservice.jipp.PrinterModel
import com.example.customeprintservice.model.DecodedJWTResponse
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.prefs.LoginPrefs.Companion.getSiteId
import com.example.customeprintservice.prefs.SignInCompanyPrefs
import com.example.customeprintservice.printjobstatus.PrinterListService
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import com.example.customeprintservice.utils.Inet
import com.example.customeprintservice.utils.IpAddress
import com.example.customeprintservice.utils.JwtDecode
import com.example.customeprintservice.utils.ProgressDialog
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_printers.*
import okhttp3.ResponseBody
import org.jetbrains.anko.doAsync
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.InetAddress

class PrintersFragment : Fragment() {

    val printerList = ArrayList<PrinterModel>()
    companion object {
        public val discoveredPrinterListWithDetails = java.util.ArrayList<PrinterModel>()
        public val serverPrinterListWithDetails = java.util.ArrayList<PrinterModel>()
        public val serverPullPrinterListWithDetails = java.util.ArrayList<PrinterModel>()
        public val serverSecurePrinterListWithDetails = java.util.ArrayList<PrinterModel>()
        public val serverSecurePrinterForHeldJob= java.util.ArrayList<PrinterModel>()
        public val allPrintersForPullHeldJob= java.util.ArrayList<PrinterModel>()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_printers, container, false)

    }

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        /*btnAddManuallyPrinter.setOnClickListener {
            dialogAddManualPrinter()
          //  dialogSelectPrinter()
        }*/

        setHasOptionsMenu(true)
        updateUi()
        getPrinterList(requireContext(), decodeJWT())
        Log.i("printer", "Login okta token" + LoginPrefs.getOCTAToken(requireContext()))
        val intent = Intent("qrcodefloatingbutton")
        intent.putExtra("qrCodeScanBtn", "InActive")
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)

        swipeContainer.setOnRefreshListener {
            getPrinterList(requireContext(), decodeJWT())

        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_search, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.serach -> {
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }


    //**************************

    @SuppressLint("WrongConstant")
    private fun updateUi() {
        val recyclerViewPrinterLst =
            view?.findViewById<RecyclerView>(R.id.recyclerViewFragmentPrinterList)

        recyclerViewPrinterLst?.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayout.VERTICAL,
                false
            )
        val adapter = FragmentPrinterListAdapter(
            context as Activity,
            PrinterList().printerList, "printerTab"
        )
        recyclerViewPrinterLst?.adapter = adapter
    }


    fun updatePrinterDialog() {

    }

    fun getPrinterList(
        context: Context, username: String
    ) {

        @SuppressLint("WrongConstant")val sh: SharedPreferences =
            context.getSharedPreferences("MySharedPref", Context.MODE_APPEND)
       // val ipAddress = IpAddress.getIPAddress(true);
        val ipAddress =IpAddress.getLocalIpAddress();
        if(ipAddress!=null) {
            Log.d("ipAddress of device:", ipAddress);
        }
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")
        var BASE_URL =""
        val companyUrl = LoginPrefs.getCompanyUrl(context)
       val siteId= LoginPrefs.getSiteId(context)
        val xIdpType =SignInCompanyPrefs.getIdpType(context)
        val xIdpName =SignInCompanyPrefs.getIdpName(context)

            BASE_URL = "https://"+companyUrl+"/client/gateway.php/"



        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call = if(IsLdap.equals("LDAP")){
            apiService.getPrinterListForLdap(
                siteId.toString(),
                LdapUsername.toString(),
                LdapPassword.toString(),
                "1",
                "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                        "<system driverless=\"1\">\n" +
                        "  <machine>\n" +
                        "    <ips>\n" +
                        "      <ip mask=\"255.255.255.0\">" + ipAddress + "</ip>\n" +
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
        }else if(siteId.toString().contains("google")){
            apiService.getPrinterListForGoogle(
                siteId.toString(),
                "Bearer ${LoginPrefs.getOCTAToken(context)}",
                username,
                xIdpType.toString(),
                xIdpName.toString(),
                "serverId",
                "1",
                "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                        "<system driverless=\"1\">\n" +
                        "  <machine>\n" +
                        "    <ips>\n" +
                        "      <ip mask=\"255.255.255.0\">" + ipAddress + "</ip>\n" +
                        "    </ips>\n" +
                        "  </machine>\n" +
                        "  <idp>\n" +
                        "    {\"idpName\": \"" + xIdpName + "\",\n" +
                        "      \"username\":\"" + username + "\",\n" +
                        "      \"isLoggedIn\": \"true\",\n" +
                        "      \"type\": \"auth-type\",\n" +
                        "      \"token\":\"" + LoginPrefs.getOCTAToken(context) + "\"}\n" +
                        "  </idp>\n" +
                        "  <memberships>\n" +
                        "    <computer />\n" +
                        "    <user>\n" +
                        "      <guid>S-1-1-0</guid>\n" +
                        "    </user>\n" +
                        "  </memberships>\n" +
                        "</system>"

            )
        }else{


            apiService.getPrinterList(
                siteId.toString(),
                "Bearer ${LoginPrefs.getOCTAToken(context)}",
                username,
                xIdpType.toString(),
                xIdpName.toString(),
                "1",
                "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                        "<system driverless=\"1\">\n" +
                        "  <machine>\n" +
                        "    <ips>\n" +
                        "      <ip mask=\"255.255.255.0\">" + ipAddress + "</ip>\n" +
                        "    </ips>\n" +
                        "  </machine>\n" +
                        "  <idp>\n" +
                        "    {\"idpName\": \"" + xIdpName + "\",\n" +
                        "      \"username\":\"" + username + "\",\n" +
                        "      \"isLoggedIn\": \"true\",\n" +
                        "      \"type\": \"auth-type\",\n" +
                        "      \"token\":\"" + LoginPrefs.getOCTAToken(context) + "\"}\n" +
                        "  </idp>\n" +
                        "  <memberships>\n" +
                        "    <computer />\n" +
                        "    <user>\n" +
                        "      <guid>S-1-1-0</guid>\n" +
                        "    </user>\n" +
                        "  </memberships>\n" +
                        "</system>"

            )
        }




        call.enqueue(object : Callback<ResponseBody> {

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                ProgressDialog.cancelLoading()
                if (response.isSuccessful) {
                    try {
                        val html = response.body()?.string()
                        //printerListForCheckIppPrinters.clear()
                        val document = Jsoup.parse(html, "", Parser.xmlParser())
                        val element = document.select("command")
                        //  val inetAddress = InetAddress.getByName("192.168.1.1")

                        PrinterList().printerList.removeIf {
                            it.fromServer == true
                        }
                        val nodeId = document.select("command")

                        nodeId.forEach {
                            Log.i("printer", "it==>${it.attr("node_id")}")
                        }
                        PrintersFragment.serverPrinterListWithDetails.clear()
                        PrintersFragment.serverPullPrinterListWithDetails.clear()
                        PrintersFragment.serverSecurePrinterListWithDetails.clear()

                        element.forEach {
                            val printerModel: PrinterModel = PrinterModel()
                            printerModel.serviceName = it.text()
                            printerModel.printerHost = InetAddress.getByName(
                                "192.168.1." + it.attr(
                                    "node_id"
                                ).toString()
                            )
                            // printerModel.printerHost =inetAddress
                            printerModel.printerPort = 631
                            printerModel.fromServer = true
                            printerModel.nodeId = it.attr("node_id").toString()
                            Log.i("printer", "html res=>${it.text()}")
                            PrinterList().addPrinterModel(printerModel)
                            //   printerListForCheckIppPrinters.add(printerModel);

                            PrinterListService().getPrinterDetails(
                                context,
                                LoginPrefs.getOCTAToken(context).toString(),
                                username,
                                SignInCompanyPrefs.getIdpType(context).toString(),
                                SignInCompanyPrefs.getIdpName(context).toString(),
                                it.attr("node_id").toString(),
                                true
                            )

                        }
                        updateUi()
                        swipeContainer.isRefreshing = false


                    } catch (e: Exception) {
                        Log.i("printer", "e=>${e.message.toString()}")
                    }
                } else {
                    ProgressDialog.cancelLoading()
                    if (swipeContainer != null) {
                        swipeContainer.isRefreshing = false
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                ProgressDialog.cancelLoading()
                if (swipeContainer != null) {
                    swipeContainer.isRefreshing = false
                }

                Log.i("printer", "Error html response==>${t.message.toString()}")
            }
        })
    }

    @SuppressLint("WrongConstant")
     fun dialogSelectPrinter() {
        val dialog = Dialog(context as Activity)
        dialog.setContentView(R.layout.dialog_select_printer)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        val window = dialog.window
        window!!.setLayout(
            AbsListView.LayoutParams.MATCH_PARENT,
            AbsListView.LayoutParams.MATCH_PARENT
        )
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        val wlp = window.attributes
        wlp.gravity = Gravity.BOTTOM
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(0.5f)
        window.attributes = wlp

        val printerRecyclerView =
            dialog.findViewById<RecyclerView>(R.id.dialogSelectPrinterRecyclerView)
        val imgCancel = dialog.findViewById<ImageView>(R.id.imgDialogSelectPrinterCancel)
        val floatButton =
            dialog.findViewById<FloatingActionButton>(R.id.dialogSelectPrinterFloatingButton)

        printerRecyclerView?.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayout.VERTICAL,
                false
            )
            val adapter = FragmentPrinterListAdapter(
                context as Activity,
                PrinterList().printerList,
                "printerTab"
            )
        printerRecyclerView?.adapter = adapter
        dialog.show()

        imgCancel.setOnClickListener {
            dialog.cancel()
        }

        floatButton.setOnClickListener {
            Toast.makeText(requireContext(), "Click on float btn", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("WrongConstant")
    private fun dialogAddManualPrinter() {
        val dialog = Dialog(context as Activity)
        dialog.setContentView(R.layout.dialog_add_manual_printer)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        dialog.window?.setLayout((6 * width) / 7, WindowManager.LayoutParams.WRAP_CONTENT)

        val edtAddManualPrinter = dialog.findViewById<EditText>(R.id.edtDialogAddManualPrinter)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnAddPrinterManually = dialog.findViewById<Button>(R.id.btnAddPrinter)
        btnCancel.setOnClickListener { dialog.dismiss() }

        val printer: PrinterModel = PrinterModel()

        btnAddPrinterManually.setOnClickListener {
            if (Inet.validIP(edtAddManualPrinter.text.toString())) {
                try {
                    var inetAddress: InetAddress? = null

                    doAsync {
                        inetAddress = InetAddress.getByName(edtAddManualPrinter.text.toString())
                    }
                    Thread.sleep(100)
                    if (inetAddress != null) {
                        printer.printerHost = inetAddress
                        printer.serviceName = "" + inetAddress
                        printer.printerPort = 631
                        printer.fromServer=false
                        printer.manual=true
                        printer.isPullPrinter="0.0"
                        Log.i("printer", "innet Address->" + inetAddress)
                    }

                } catch (e: Exception) {
                    Log.i("printer", e.toString())
                }

                var flagIsExist: Boolean = false

                PrinterList().printerList.forEach {
                    if (it.printerHost.equals(printer.printerHost)) {
                        flagIsExist = true
                    }
                }

                if (!flagIsExist) {
                    PrinterList().addPrinterModel(printer)
                    Toast.makeText(context, "Printer Added", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    updateUi()
                } else {
                    Toast.makeText(context, "Unable to add Printer", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "IP is not valid", Toast.LENGTH_SHORT).show()
            }
        }
    }



    fun decodeJWT(): String {
        var userName: String? = null
        try {
            val mapper = jacksonObjectMapper()
            val decoded: DecodedJWTResponse = mapper.readValue<DecodedJWTResponse>(
                LoginPrefs.getOCTAToken(requireContext())?.let { JwtDecode.decoded(it) }!!
            )
            userName = decoded.user.toString()
            if(decoded.email!=null) {
                userName = decoded.email.toString()
            }
        } catch (ex: Exception) {
            //context.toast("Failed to Decode Jwt Token")
        }
        return userName.toString()
    }


    fun decodeJWT(context: Context): String {
        var userName: String? = null
        try {
            val mapper = jacksonObjectMapper()
            val decoded: DecodedJWTResponse = mapper.readValue<DecodedJWTResponse>(
                LoginPrefs.getOCTAToken(context)?.let { JwtDecode.decoded(it) }!!
            )
            userName = decoded.user.toString()
            if(decoded.email!=null) {
                userName = decoded.email.toString()
            }
        } catch (ex: Exception) {
            Log.d("exception", ex.toString())
        }
        return userName.toString()
    }

    fun getPrinterListByPrinterId(
        context: Context, printerId: String, purpose: String
    ) {

        @SuppressLint("WrongConstant")val sh: SharedPreferences = context.getSharedPreferences(
            "MySharedPref",
            Context.MODE_APPEND
        )
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")

        val siteId = getSiteId(context)
        val BASE_URL =
            "https://gw.app.printercloud.com/"+siteId+"/prs/v1/printers/"+printerId+"/"

        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)
        val call = if(IsLdap.equals("LDAP")){
            apiService.getPrinterDetailsByPrinterIdForLdap(
                siteId.toString(),
                LdapUsername.toString(),
                LdapPassword.toString()
            )
        }else if(siteId.toString().contains("google")){
            apiService.getPrinterDetailsByPrinterIdForGoogle(
                LoginPrefs.getOCTAToken(context).toString(),
                decodeJWT(context),
                SignInCompanyPrefs.getIdpType(context).toString(),
                SignInCompanyPrefs.getIdpName(context).toString(),
                "serverId"
            )
        }
        else{
            apiService.getPrinterDetailsByPrinterId(
                LoginPrefs.getOCTAToken(context).toString(),
                decodeJWT(context),
                SignInCompanyPrefs.getIdpType(context).toString(),
                SignInCompanyPrefs.getIdpName(context).toString()
            )
        }

        call?.enqueue(object : Callback<Any> {

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<Any>,
                response: Response<Any>
            ) {
                ProgressDialog.cancelLoading()
                if (response.isSuccessful) {

                    Log.d("response of printerId:", response.body().toString())
                    var s = response.body().toString()
                    s = s.replace("\"", "")
                    val hashMap: HashMap<String, String> = HashMap<String, String>()
                    val pairs = s.split(",".toRegex()).toTypedArray()
                    for (i in pairs.indices) {
                        val pair = pairs[i]
                        val keyValue =
                            pair.split("=".toRegex()).toTypedArray()
                        if (keyValue.size > 1) {
                            if (keyValue[0].trim().equals("pull-print")) {
                                if (!hashMap.containsKey("pull-print")) {
                                    hashMap.put(keyValue[0].trim(), keyValue[1])
                                }
                            } else {
                                hashMap.put(keyValue[0].trim(), keyValue[1])
                            }
                        }
                    }
                    // Log.d("response of printerId:",response.body()?.data?.attributes?.host-address.toString())
                    val title = hashMap.get("title")
                    val hostAddress = hashMap.get("host-address")
                    val isPullPrinter = hashMap.get("is-pull-printer")
                    val printerToken = hashMap.get("printer-token")
                    val pull_print = hashMap.get("pull-print")
                    val id = hashMap.get("id")
                    Log.d("title", title.toString())
                    Log.d("hostAddress", hostAddress.toString())
                    Log.d("isPullPrinter", isPullPrinter.toString())
                    ServerPrintRelaseFragment.selectedPrinterId = id
                    ServerPrintRelaseFragment.selectedPrinterToken = printerToken
                    val printer: PrinterModel = PrinterModel()
                    printer.id = id
                    val thread = Thread(Runnable {
                        try {
                            printer.printerHost = InetAddress.getByName(hostAddress)
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                        }
                    })

                    thread.start()



                    printer.serviceName = title
                    printer.printerPort = 631
                    printer.manual = true
                    printer.fromServer = false
                    printer.isPullPrinter = isPullPrinter.toString()
                    printer.pull_print = pull_print;
                    var flagIsExist: Boolean = false
                    //when select one document then only get printer by using queue id for display in dialog box
                    if (purpose.equals("forSecureRelase")) {
                        printer.manual = false
                        printer.fromServer = true
                        serverSecurePrinterForHeldJob.clear()
                        serverSecurePrinterForHeldJob.add(printer)
                    }

                    if (purpose.equals("printerDetailForAddPrinterTab")) {

                        PrinterList().printerList.forEach {
                            if (it.serviceName.equals(printer.serviceName)) {
                                flagIsExist = true
                            }
                        }

                        if (!flagIsExist) {
                            PrinterList().addPrinterModel(printer)
                            addPrinterForshareDocument(printer, context)
                            Toast.makeText(context, "Printer Added", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Unable to add Printer", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }

                    if (purpose.equals("getPrinterDetailsForPullJob")) {
                        if (printer.isPullPrinter.equals("0.0") && (printer.pull_print.equals("2.0") || printer.pull_print.equals(
                                "0.0"
                            ))
                        ) {
                            printer.manual = false
                            allPrintersForPullHeldJob.add(printer);
                        }
                    }

                    //**********

                }

            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                ProgressDialog.cancelLoading()

            }
        })
    }



    fun addPrinterForshareDocument(printer: PrinterModel, context: Context) {
        if(printer.isPullPrinter.equals("0.0")) {
            var serverSecurePrinterListWithDetailsSharedPreflist = java.util.ArrayList<PrinterModel>()
            val prefs1 = PreferenceManager.getDefaultSharedPreferences(context)
            val gson1 = Gson()
            val json2 = prefs1.getString("prefServerSecurePrinterListWithDetails", null)
            val type1 = object :
                TypeToken<java.util.ArrayList<PrinterModel?>?>() {}.type
            if (json2 != null) {
                serverSecurePrinterListWithDetailsSharedPreflist =
                    gson1.fromJson<java.util.ArrayList<PrinterModel>>(json2, type1)
                serverSecurePrinterListWithDetailsSharedPreflist.add(printer)

                val editor = prefs1.edit()
                val json1 = gson1.toJson(serverSecurePrinterListWithDetailsSharedPreflist)
                editor.putString("prefServerSecurePrinterListWithDetails", json1)
                editor.apply()

            }
        }
    }
}
