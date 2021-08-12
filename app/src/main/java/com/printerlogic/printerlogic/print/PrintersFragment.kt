package com.printerlogic.printerlogic.print

//import org.slf4j.LoggerFactory
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.os.*
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.printerlogic.printerlogic.MainActivity
import com.printerlogic.printerlogic.R
import com.printerlogic.printerlogic.adapter.FragmentPrinterListAdapter
import com.printerlogic.printerlogic.jipp.PrinterList
import com.printerlogic.printerlogic.jipp.PrinterModel
import com.printerlogic.printerlogic.model.DecodedJWTResponse
import com.printerlogic.printerlogic.prefs.LoginPrefs
import com.printerlogic.printerlogic.prefs.LoginPrefs.Companion.getSiteId
import com.printerlogic.printerlogic.prefs.LoginPrefs.Companion.getTenantUrl
import com.printerlogic.printerlogic.prefs.SignInCompanyPrefs
import com.printerlogic.printerlogic.printjobstatus.PrinterListService
import com.printerlogic.printerlogic.rest.ApiService
import com.printerlogic.printerlogic.rest.RetrofitClient
import com.printerlogic.printerlogic.utils.*
import com.printerlogic.printerlogic.utils.ProgressDialog.Companion.cancelLoading
import com.printerlogic.printerlogic.utils.ProgressDialog.Companion.showLoadingDialog
import kotlinx.android.synthetic.main.fragment_printers.*
import okhttp3.ResponseBody
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.InetAddress
import java.net.URLEncoder
import java.util.function.Consumer


class PrintersFragment : Fragment() {

    val printerList = ArrayList<PrinterModel>()
    var isSwipeCompleted:Boolean =true


    companion object {
         val discoveredPrinterListWithDetails = java.util.ArrayList<PrinterModel>()
         val serverPrinterListWithDetails = java.util.ArrayList<PrinterModel>()
         val serverPullPrinterListWithDetails = java.util.ArrayList<PrinterModel>()
         val serverSecurePrinterListWithDetails = java.util.ArrayList<PrinterModel>()
         val serverSecurePrinterForHeldJob= java.util.ArrayList<PrinterModel>()
         val allPrintersForPullHeldJob= java.util.ArrayList<PrinterModel>()
        val deployedPrintersListForPrintPreivew =java.util.ArrayList<PrinterModel>()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState);
        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(mMessageReceiver, IntentFilter("callUpdateUIMethod"))



    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_printers, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       // setHasOptionsMenu(true)

        (activity as AppCompatActivity).getSupportActionBar()?.setHomeButtonEnabled(true)
        (activity as AppCompatActivity).getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        (activity as AppCompatActivity).getSupportActionBar()?.setHomeAsUpIndicator(R.drawable.drawericon1)

        search.visibility=View.GONE
        clear.visibility=View.GONE
        updateUi(PrinterList().printerList, requireContext(), "")

        val intent = Intent("qrcodefloatingbutton")
        intent.putExtra("qrCodeScanBtn", "InActive")
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)

        swipeContainer.setOnRefreshListener {

            if(isSwipeCompleted==true) {
                PrinterList().printerList.clear()
                updateUi(PrinterList().printerList, requireContext(), "")
                getPrinterList(requireContext(), decodeJWT())
                val handler = Handler()
                handler.postDelayed({
                    val mainActivity = MainActivity()
                    try {
                        mainActivity.getAttributeDeatilsForNativePrint(requireContext())
                    } catch (e: Exception) {
                        DataDogLogger.getLogger().i(
                            "Devnco_Android :" + e.message
                        )
                    }
                }, 7000)




                object : CountDownTimer(120000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        // textView.setText("seconds remaining: " + millisUntilFinished / 1000)
                        isSwipeCompleted=false;
                    }

                    override fun onFinish() {
                        //  textView.setText("done!")
                        isSwipeCompleted =true
                    }
                }.start()


            }else{
                if (swipeContainer != null) {
                    swipeContainer.isRefreshing = false
                }
            }



        }

        clear.setOnClickListener {
            search.visibility=View.GONE;
            clear.visibility=View.GONE
        }

        search.addTextChangedListener(watcher)
    }



    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("WrongConstant")
     fun updateUi(list: java.util.ArrayList<PrinterModel>, context: Context, char: String) {

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val gson = Gson()
        val json = prefs.getString("prefaddedPrinterListWithDetails", null)

        @SuppressLint("WrongConstant")val sh: SharedPreferences = context.getSharedPreferences(
            "MySharedPref",
            Context.MODE_APPEND
        )
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")


        val type = object :
            TypeToken<java.util.ArrayList<PrinterModel?>?>() {}.type
        var sharedPreferencesStoredPrinterListWithDetails = java.util.ArrayList<PrinterModel>()
        if (json != null) {
            sharedPreferencesStoredPrinterListWithDetails = gson.fromJson<java.util.ArrayList<PrinterModel>>(
                json,
                type
            )
        }

        if (sharedPreferencesStoredPrinterListWithDetails != null && sharedPreferencesStoredPrinterListWithDetails.size > 0) {
            sharedPreferencesStoredPrinterListWithDetails.forEach(Consumer { p: PrinterModel ->
                var isAvailable: Boolean = false
                list.forEach(Consumer { deployedPrinter: PrinterModel ->
                    if (p.serviceName.equals(deployedPrinter.serviceName)) {
                        isAvailable = true;
                    }
                })
                if (isAvailable == false) {
                    if(p.printerAddedByUser.equals(decodeJWT(context) )  && p.idpName.equals(SignInCompanyPrefs.getIdpName(context))) {
                        list.add(p)
                    }else if(p.printerAddedByUser.equals(LdapUsername) && p.idpName.equals(IsLdap)){
                        list.add(p)
                    }
                }

            })


        }

        val sortedList:ArrayList<PrinterModel> = PrinterListComparator.getSortedPrinterList(list)
        val recyclerViewPrinterLst = view?.findViewById<RecyclerView>(R.id.recyclerViewFragmentPrinterList)
        recyclerViewPrinterLst?.layoutManager = LinearLayoutManager(
            context,
            LinearLayout.VERTICAL,
            false
        )

        if (recyclerViewPrinterLst != null) {
            recyclerViewPrinterLst.setItemViewCacheSize(100)
        }

        val adapter = FragmentPrinterListAdapter(
            context,
            sortedList,
            "printerTab"
        )
        recyclerViewPrinterLst?.adapter = adapter


        val recyclerViewAlphabetsList = view?.findViewById<RecyclerView>(R.id.alphabetsRecyclerView)
        recyclerViewAlphabetsList?.layoutManager = LinearLayoutManager(
            context,
            LinearLayout.VERTICAL,
            false
        )
          }


    fun getPrinterList(context: Context, username: String){
        @SuppressLint("WrongConstant")val sh: SharedPreferences = context.getSharedPreferences(
            "MySharedPref",
            Context.MODE_APPEND
        )
        val ipAddress =IpAddress.getLocalIpAddress();

        if(ipAddress!=null) {
            Log.d("ipAddress of device:", ipAddress);
            DataDogLogger.getLogger().i("Devnco_Android ipAddress of device:" + ipAddress);
        }
        showLoadingDialog(context, "please wait")
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")
        var BASE_URL =""
        val companyUrl = LoginPrefs.getCompanyUrl(context)
        val siteId= LoginPrefs.getSiteId(context)
        val xIdpType =SignInCompanyPrefs.getIdpType(context)
        val xIdpName =SignInCompanyPrefs.getIdpName(context)

     //   BASE_URL = "https://"+companyUrl+"/client/gateway.php/"
        BASE_URL = "https://"+companyUrl+"/api/mobile/client-gateway/"
        val apiService = RetrofitClient(context).getRetrofitInstance(BASE_URL).create(ApiService::class.java)

        val call = if(IsLdap.equals("LDAP")){
            val sessionId = LoginPrefs.getSessionIdForLdap(context)
            apiService.getPrinterListForLdap(
                siteId.toString(),
                LdapUsername.toString(),
                LdapPassword.toString(),
                "PHPSESSID=" + sessionId,
                "1",
                "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                        "<system driverless=\"1\">\n" +
                        "  <machine>\n" +
                        "    <ips>\n" +
                        "      <ip mask=\"255.255.255.0\">" + ipAddress + "</ip>\n" +
                        "    </ips>\n" +
                        "  </machine>\n" +
                        "  <memberships>\n" +
                        "    <computer />\n" +
                        "    <user>\n" +
                        "      <guid>S-1-1-0</guid>\n" +
                        "    </user>\n" +
                        "  </memberships>\n" +
                        "</system>"

            )
        }else if(xIdpType.toString().toLowerCase().equals("oidc")){
            DataDogLogger.getLogger().i(
                "Devnco_Android API call: " + BASE_URL.toString() + " Token: " + LoginPrefs.getOCTAToken(
                    context
                ) + " username: " + username
            )
            val idpInfo ="\"clientType\":"+"\"serverId\""+",\"idpName\":"+"\""+xIdpName+"\",\"username\":"+"\""+username+"\",\"isLoggedIn\":"+"\""+true+"\",\"type\":"+"\""+xIdpType+"\",\"token\":"+"\""+LoginPrefs.getOCTAToken(
                context
            )+"\"";
            Log.d("encodedIdpInfo: ", URLEncoder.encode(idpInfo))
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
                        "{" + URLEncoder.encode(idpInfo) + "}" +
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
            DataDogLogger.getLogger().i(
                "Devnco_Android API call: " + BASE_URL.toString() + " Token: " + LoginPrefs.getOCTAToken(
                    context
                ) + " username: " + username
            )

            val idpInfo ="\"clientType\":"+"\"serverId\""+",\"idpName\":"+"\""+xIdpName+"\",\"username\":"+"\""+username+"\",\"isLoggedIn\":"+"\""+true+"\",\"type\":"+"\""+xIdpType+"\",\"token\":"+"\""+LoginPrefs.getOCTAToken(
                context
            )+"\"";

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
                        "{" + URLEncoder.encode(idpInfo) + "}" +
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
                //   ProgressDialog.cancelLoading()
                if (response.isSuccessful) {
                    try {
                        val html = response.body()?.string()
                        val document = Jsoup.parse(html, "", Parser.xmlParser())
                        val element = document.select("command")
                        PrinterList().printerList.removeIf {
                            it.fromServer == true
                        }
                        val nodeId = document.select("command")

                        nodeId.forEach {
                            Log.i("printer", "it==>${it.attr("node_id")}")
                            DataDogLogger.getLogger()
                                .i("Devnco_Android printer" + "it==>${it.attr("node_id")}")
                        }
                        PrintersFragment.serverPrinterListWithDetails.clear()
                        PrintersFragment.serverPullPrinterListWithDetails.clear()
                        PrintersFragment.serverSecurePrinterListWithDetails.clear()
                        PrintersFragment.deployedPrintersListForPrintPreivew.clear()

                        element.forEach {
                            val printerModel: PrinterModel = PrinterModel()
                            printerModel.serviceName = it.text()
                            printerModel.printerHost = InetAddress.getByName(
                                "192.168.1.10"
                            )
                            printerModel.printerPort = 631
                            printerModel.fromServer = true
                            printerModel.nodeId = it.attr("node_id").toString()
                            Log.i("printer", "html res=>${it.text()}")
                            DataDogLogger.getLogger()
                                .i("Devnco_Android printer" + "html res=>${it.text()}")


                            val thread = Thread(Runnable {
                                try {
                                    PrinterListService().getPrinterDetails(
                                        context, LoginPrefs.getOCTAToken(
                                            context
                                        ).toString(), username,
                                        SignInCompanyPrefs.getIdpType(context).toString(),
                                        SignInCompanyPrefs.getIdpName(context).toString(),
                                        it.attr("node_id").toString(),
                                        true
                                    )
                                } catch (e: java.lang.Exception) {
                                    e.printStackTrace()
                                }
                            })

                            thread.start()


                        }

                        if (element.size == 0) {
                            removePreferencesDeployedPrinters(context)
                        }


                        Handler().postDelayed({
                            updateUi(PrinterList().printerList, context, "")
                            if (swipeContainer != null) {
                                swipeContainer.isRefreshing = false
                            }


                        }, 8000)
                        cancelLoading()

                    } catch (e: Exception) {
                        Log.i("printer", "e=>${e.message.toString()}")
                        DataDogLogger.getLogger()
                            .e("Devnco_Android printer" + "e=>${e.message.toString()}")
                        ProgressDialog.cancelLoading()
                        if (swipeContainer != null) {
                            swipeContainer.isRefreshing = false
                        }
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
                DataDogLogger.getLogger()
                    .i("Devnco_Android printer" + "Error html response==>${t.message.toString()}")
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

        printerRecyclerView?.layoutManager = LinearLayoutManager(
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

        }
    }



    fun decodeJWT(): String {
        var userName: String? = null
        try {
            val mapper = jacksonObjectMapper()
            val decoded: DecodedJWTResponse = mapper.readValue<DecodedJWTResponse>(
                LoginPrefs.getOCTAToken(
                    requireContext()
                )?.let { JwtDecode.decoded(it) }!!
            )
            userName = decoded.user.toString()
            if(decoded.email!=null) {
                userName = decoded.email.toString()
            }

            val sharedPreferences: SharedPreferences =
                requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
            val myEdit = sharedPreferences.edit()
            myEdit.putString("userName", userName);
            myEdit.commit()
        } catch (ex: Exception) {

        }
        return userName.toString()
    }


    fun decodeJWT(context: Context): String {
        var userName: String? = null
        try {
            val mapper = jacksonObjectMapper()
            val decoded: DecodedJWTResponse = mapper.readValue<DecodedJWTResponse>(
                LoginPrefs.getOCTAToken(
                    context
                )?.let { JwtDecode.decoded(it) }!!
            )
            userName = decoded.user.toString()
            if(decoded.email!=null) {
                userName = decoded.email.toString()
            }

            val sharedPreferences: SharedPreferences = context.getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
            val myEdit = sharedPreferences.edit()
            myEdit.putString("userName", userName);
            myEdit.commit()
        } catch (ex: Exception) {
            Log.d("exception", ex.toString())
            DataDogLogger.getLogger().e("Devnco_Android exception" + ex.toString())
        }
        return userName.toString()
    }

    fun getPrinterListByPrinterId(context: Context, printerId: String, purpose: String) {
        @SuppressLint("WrongConstant")val sh: SharedPreferences = context.getSharedPreferences(
            "MySharedPref",
            Context.MODE_APPEND
        )


        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")
        val siteId = getSiteId(context)
        val tanentUrl = getTenantUrl(context)
        val BASE_URL = ""+tanentUrl+"/"+siteId+"/prs/v1/printers/"+printerId+"/"
        //val BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/prs/v1/printers/"+printerId+"/"

        val apiService = RetrofitClient(context).getRetrofitInstance(BASE_URL).create(ApiService::class.java)
        val call = if(IsLdap.equals("LDAP")){
            val sessionId = LoginPrefs.getSessionIdForLdap(context)
            apiService.getPrinterDetailsByPrinterIdForLdap(
                siteId.toString(),
                LdapUsername.toString(),
                LdapPassword.toString(),
                "PHPSESSID=" + sessionId
            )
        }else if(SignInCompanyPrefs.getIdpType(context).toString().toLowerCase().equals("oidc")){
            DataDogLogger.getLogger().i(
                "Devnco_Android API call: " + BASE_URL.toString() + " Token: " + "Bearer " + LoginPrefs.getOCTAToken(
                    context
                ).toString() + " username: " + decodeJWT(context)
            )
                apiService.getPrinterDetailsByPrinterIdForGoogle(
                    "Bearer " + LoginPrefs.getOCTAToken(context).toString(),
                    decodeJWT(context),
                    SignInCompanyPrefs.getIdpType(context).toString(),
                    SignInCompanyPrefs.getIdpName(context).toString(),
                    "serverId"
                )
        }
        else{
            DataDogLogger.getLogger().i(
                "Devnco_Android API call: " + BASE_URL.toString() + " Token: " + LoginPrefs.getOCTAToken(
                    context
                ).toString() + " username: " + decodeJWT(context)
            )
            apiService.getPrinterDetailsByPrinterId(
                "Bearer " + LoginPrefs.getOCTAToken(context).toString(),
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

                if (response.isSuccessful) {
                    val thread = Thread(Runnable {
                        Log.d("response of printerId:", response.body().toString())
                        DataDogLogger.getLogger().i(
                            "Devnco_Android response of printerId:" + response.body().toString()
                        )
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
                        val title = hashMap.get("title")
                        val hostAddress = hashMap.get("host-address")
                        val isPullPrinter = hashMap.get("is-pull-printer")
                        val printerToken = hashMap.get("printer-token")
                        val pull_print = hashMap.get("pull-print")
                        val is_Color = hashMap.get("is-color")
                        val secureRelease = hashMap.get("secure-release")
                        var secureReleaseId=""
                        if (secureRelease.equals("0.0")) {
                            secureReleaseId="0"
                        }else if (secureRelease.equals("1.0")) {
                            secureReleaseId="1"
                        }else if (secureRelease.equals("2.0")) {
                            secureReleaseId="2"
                        }else if (secureRelease.equals("3.0")) {
                            secureReleaseId="3"
                        }else if (secureRelease.equals("4.0")) {
                            secureReleaseId="4"
                        }else if (secureRelease.equals("5.0")) {
                            secureReleaseId="5"
                        }else if (secureRelease.equals("6.0")) {
                            secureReleaseId="6"
                        }



                        val isColor: Int
                        if (is_Color.equals("0.0")) {
                            isColor = 0
                        } else {
                            isColor = 1
                        }
                        val location: String = hashMap.getOrDefault("location", "")
                        val id = hashMap.get("id")
                        Log.d("title", title.toString())
                        DataDogLogger.getLogger().i("Devnco_Android title:" + title.toString())
                        Log.d("hostAddress", hostAddress.toString())
                        DataDogLogger.getLogger()
                            .i("Devnco_Android hostAddress:" + hostAddress.toString())
                        Log.d("isPullPrinter", isPullPrinter.toString())
                        DataDogLogger.getLogger()
                            .i("Devnco_Android isPullPrinter:" + isPullPrinter.toString())
                        ServerPrintRelaseFragment.selectedPrinterId = id
                        ServerPrintRelaseFragment.selectedPrinterToken = printerToken
                        ServerPrintRelaseFragment.selectedPrinterHost = hostAddress.toString()
                        ServerPrintRelaseFragment.selectedPrinterServiceName = title.toString()
                        val printer: PrinterModel = PrinterModel()
                        printer.id = id
                        printer.secure_release = secureReleaseId.toString()
                        if (isPullPrinter.equals("1.0") || isPullPrinter.equals("1")) {
                            printer.printerHost = InetAddress.getByName("192.168.1." + id)
                        } else {

                            try {
                                printer.printerHost = InetAddress.getByName(hostAddress)

                                // show error
                                Handler(Looper.getMainLooper()).post {
                                /*    Toast.makeText(
                                        context,
                                        "host:: " + printer.printerHost.hostAddress,
                                        Toast.LENGTH_LONG
                                    ).show();

                                 */
                                }

                            } catch (e: java.lang.Exception) {
                                Handler(Looper.getMainLooper()).post {
                                 /*   Toast.makeText(
                                        context,
                                        "exception:: " + e.message,
                                        Toast.LENGTH_LONG
                                    ).show();

                                  */
                                }

                                e.printStackTrace()
                            }


                        }

                        printer.serviceName = title
                        printer.printerPort = 631
                        printer.manual = true
                        printer.fromServer = false
                        printer.isPullPrinter = isPullPrinter.toString()
                        printer.pull_print = pull_print;
                        printer.isColor = isColor
                        printer.location = location
                        var flagIsExist: Boolean = false

                        if (purpose.equals("forSecureRelase")) {
                            printer.manual = false
                            printer.fromServer = true
                            serverSecurePrinterForHeldJob.clear()
                            serverSecurePrinterForHeldJob.add(printer)
                        }

                        if (purpose.equals("getprinterToken")) {
                            BottomNavigationActivityForServerPrint.selectedPrinter.serviceName =
                                title
                            BottomNavigationActivityForServerPrint.selectedPrinter.printerHost =
                                InetAddress.getByName(
                                    hostAddress
                                )
                            BottomNavigationActivityForServerPrint.selectedPrinter.id = printerId
                        }

                        if (purpose.equals("printerDetailForAddPrinterTab")) {
                            try {
                                DataDogLogger.getLogger().i(
                                    "Devnco_Android API call for Add printer : " + BASE_URL.toString() + " Token: " + "Bearer " + LoginPrefs.getOCTAToken(
                                        context
                                    ).toString() + " username: " + decodeJWT(context)
                                )
                            }catch(e: java.lang.Exception){
                                DataDogLogger.getLogger().i(
                                    "Devnco_Android Add Printer API datadog exception : " + e.message
                                )
                            }




                            PrinterList().printerList.forEach {
                                if (it.serviceName.equals(printer.serviceName)) {
                                    flagIsExist = true
                                }
                            }

                            if (!flagIsExist) {
                                DataDogLogger.getLogger().i(
                                    "Devnco_Android Add Printer check Isavailble  : " + flagIsExist.toString()
                                )
                                if (printer.printerHost.hostAddress != null) {
                                    DataDogLogger.getLogger().i(
                                        "Devnco_Android Add Printer host addresss  : " + printer.printerHost.hostAddress.toString()
                                    )

                                    printer.printerAddedByUser=decodeJWT(context)
                                    printer.idpName=SignInCompanyPrefs.getIdpName(context).toString()
                                    if(IsLdap.equals("LDAP")){
                                        printer.printerAddedByUser=LdapUsername.toString()
                                        printer.idpName="LDAP"
                                    }
                                    PrinterList().addPrinterModel(printer)
                                    addPrinterForshareDocument(printer, context)
                                    DataDogLogger.getLogger().i(
                                        "Devnco_Android Add Printer   : " +"Printer Added"
                                    )
                                    Handler(Looper.getMainLooper()).post {
                                        Toast.makeText(context, "Printer Added", Toast.LENGTH_SHORT)
                                            .show()
                                    }
                                }
                            } else {
                                DataDogLogger.getLogger().i(
                                    "Devnco_Android Add Printer check printer Isavailable  : " + flagIsExist.toString()
                                )
                                DataDogLogger.getLogger().i(
                                    "Devnco_Android Add Printer   : " +"Printer Already Added"
                                )
                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(
                                        context,
                                        "Printer Already Added",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
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
                    })
                    thread.start()
                    ProgressDialog.cancelLoading()
                }

            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                ProgressDialog.cancelLoading()

            }
        })
    }



    fun addPrinterForshareDocument(printer: PrinterModel, context: Context) {
     //   if(printer.isPullPrinter.equals("0.0")) {
            var serverSecurePrinterListWithDetailsSharedPreflist = java.util.ArrayList<PrinterModel>()
            val prefs5 = PreferenceManager.getDefaultSharedPreferences(context)
            val gson5 = Gson()
            val json5 = prefs5.getString("prefServerSecurePrinterListWithDetails", null)
            val type5 = object :
                TypeToken<java.util.ArrayList<PrinterModel?>?>() {}.type
            if (json5 != null) {
                serverSecurePrinterListWithDetailsSharedPreflist = gson5.fromJson<java.util.ArrayList<PrinterModel>>(
                    json5,
                    type5
                )
                serverSecurePrinterListWithDetailsSharedPreflist.add(printer)
                val editor = prefs5.edit()
                val json6 = gson5.toJson(serverSecurePrinterListWithDetailsSharedPreflist)
                editor.putString("prefServerSecurePrinterListWithDetails", json6)
                editor.apply()

            }else{
                serverSecurePrinterListWithDetailsSharedPreflist.add(printer)
                val editor = prefs5.edit()
                val json6 = gson5.toJson(serverSecurePrinterListWithDetailsSharedPreflist)
                editor.putString("prefServerSecurePrinterListWithDetails", json6)
                editor.apply()
            }
       // }


        var addedListWithDetailsSharedPreflist = java.util.ArrayList<PrinterModel>()
        val prefs1 = PreferenceManager.getDefaultSharedPreferences(context)
        val gson1 = Gson()
        val json2 = prefs1.getString("prefaddedPrinterListWithDetails", null)
        val type1 = object :
            TypeToken<java.util.ArrayList<PrinterModel?>?>() {}.type
        if (json2 != null) {
            addedListWithDetailsSharedPreflist = gson1.fromJson<java.util.ArrayList<PrinterModel>>(
                json2,
                type1
            )
            addedListWithDetailsSharedPreflist.add(printer)
            val editor = prefs1.edit()
            val json1 = gson1.toJson(addedListWithDetailsSharedPreflist)
            editor.putString("prefaddedPrinterListWithDetails", json1)
            editor.apply()

        }else{
            addedListWithDetailsSharedPreflist.add(printer)
            val editor = prefs1.edit()
            val json1 = gson1.toJson(addedListWithDetailsSharedPreflist)
            editor.putString("prefaddedPrinterListWithDetails", json1)
            editor.apply()
        }
    }

    val watcher: TextWatcher = object : TextWatcher {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun afterTextChanged(s: Editable) {
            Log.d("text:", s.toString())
            DataDogLogger.getLogger().i("Devnco_Android text:" + s.toString())
            val filterList = java.util.ArrayList<PrinterModel>()
            for (i in PrinterList().printerList.indices) {
                val printerModel: PrinterModel = PrinterList().printerList.get(i)
                if (printerModel.serviceName.toLowerCase()
                        .contains(s.toString().toLowerCase())
                ) {
                    filterList.add(printerModel)
                }
            }

            updateUi(filterList, requireContext(), "")
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    }

   fun removePrinters(context: Context){
       var emptyList = java.util.ArrayList<PrinterModel>()
       val prefs1 = PreferenceManager.getDefaultSharedPreferences(context)
       val gson1 = Gson()
           val editor = prefs1.edit()
           val json1 = gson1.toJson(emptyList)
          // editor.putString("prefServerSecurePrinterListWithDetails", json1)
       editor.putString("deployedPrintersListForPrintPreivew", json1)
     //  editor.putString("prefaddedPrinterListWithDetails", json1)
       editor.commit()



   }

    fun removePreferencesDeployedPrinters(context: Context){
        var emptyList = java.util.ArrayList<PrinterModel>()
        val prefs1 = PreferenceManager.getDefaultSharedPreferences(context)
        val gson1 = Gson()
        val editor = prefs1.edit()
        val json1 = gson1.toJson(emptyList)
        editor.putString("deployedsecurePrinterListWithDetails", json1)
        editor.apply()


    }

    var mMessageReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun onReceive(context: Context, intent: Intent) {
            updateUi(PrinterList().printerList, context, "")
        }
    }

}
