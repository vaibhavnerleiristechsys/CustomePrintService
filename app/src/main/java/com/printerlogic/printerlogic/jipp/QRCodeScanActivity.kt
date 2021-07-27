package com.printerlogic.printerlogic.jipp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.budiyev.android.codescanner.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.printerlogic.printerlogic.MainActivity
import com.printerlogic.printerlogic.R
import com.printerlogic.printerlogic.model.DecodedJWTResponse
import com.printerlogic.printerlogic.prefs.LoginPrefs
import com.printerlogic.printerlogic.prefs.LoginPrefs.Companion.getSessionIdForLdap
import com.printerlogic.printerlogic.prefs.LoginPrefs.Companion.getTenantUrl
import com.printerlogic.printerlogic.prefs.SignInCompanyPrefs
import com.printerlogic.printerlogic.print.*
import com.printerlogic.printerlogic.printjobstatus.model.getjobstatuses.GetJobStatusesResponse
import com.printerlogic.printerlogic.printjobstatus.model.getjobstatuses.PrintQueueJobStatusItem
import com.printerlogic.printerlogic.rest.ApiService
import com.printerlogic.printerlogic.rest.RetrofitClient
import com.printerlogic.printerlogic.room.SelectedFile
import com.printerlogic.printerlogic.utils.DataDogLogger
import com.printerlogic.printerlogic.utils.JwtDecode
import com.printerlogic.printerlogic.utils.ProgressDialog
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_q_r_code_scan.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class QRCodeScanActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private val CAMERA_REQUEST_CODE=123;
    private var printerId="";
    lateinit var printerRecyclerView :RecyclerView;
    lateinit var  emptyviewForQrCode :ConstraintLayout;
    companion object {
         val getdocumentListFromQrCode = java.util.ArrayList<SelectedFile>()
    }

    var floatButton: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_q_r_code_scan)

        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)

        val extras = intent.extras
        var location:String=""
        var printerId:String=""
        if (extras != null) {
             location = extras.getString("startqrcodescan").toString()
            printerId  = extras.getString("printerId").toString()

        }

        if(location.equals("startqrcodescan")) {
            scannerView.visibility=View.GONE
            backbutton.visibility =View.GONE
            if (printerId != null) {
                getJobListByPrinterId(this, printerId)
            }
            PrintersFragment().getPrinterListByPrinterId(
                this,
                printerId.toString(),
                "getprinterToken"
            )
        }
        val permisison2 = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        if (permisison2 != PackageManager.PERMISSION_GRANTED) {
            makeRequest2()
        }

        codeScanner = CodeScanner(this, scannerView)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                // Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
                var string = it.text
                var printerId = string.filter { it.isDigit() }
                getJobListByPrinterId(this, printerId)
                PrintersFragment().getPrinterListByPrinterId(
                    this,
                    printerId.toString(),
                    "getprinterToken"
                )

            }
        }
        codeScanner.errorCallback = ErrorCallback {
            runOnUiThread {
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }



        LocalBroadcastManager.getInstance(this).registerReceiver(
            mMessageReceiver1,
            IntentFilter("menuFunctionlityDisplay")
        )


        backbutton.setOnClickListener {
             val intent = Intent(this@QRCodeScanActivity, MainActivity::class.java)
             startActivity(intent)

        }

    }
        override fun onResume() {
            super.onResume()
            if(codeScanner!=null) {
                codeScanner.startPreview()
            }
        }

        override fun onPause() {
            codeScanner.releaseResources()
            super.onPause()
        }


        private fun makeRequest2() {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
        }


    override fun onBackPressed() {

        val intent = Intent(this@QRCodeScanActivity, MainActivity::class.java)
        startActivity(intent)
    }


    fun getJobListByPrinterId(context: Context, printerId: String) {

        @SuppressLint("WrongConstant")val sh: SharedPreferences = context.getSharedPreferences(
            "MySharedPref",
            Context.MODE_APPEND
        )
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")

        Log.d("IsLdap:", IsLdap!!)
        val siteId= LoginPrefs.getSiteId(context)

        val tanentUrl = getTenantUrl(context)
        var BASE_URL = ""+tanentUrl+"/"+siteId+"/pq/api/job-statuses/"
        //var BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/pq/api/job-statuses/"

        val apiService = RetrofitClient(context).getRetrofitInstance(BASE_URL).create(ApiService::class.java)

        val call = if(IsLdap.equals("LDAP")){
            val sessionId = getSessionIdForLdap(context)
            apiService.getPrintJobStatusesForLdap(
                "devncoldap",
                LdapUsername.toString(),
                LdapPassword.toString(),
                "printerDeviceQueue.printers",
                "PHPSESSID=" + sessionId
                )
        }else{
            apiService.getPrintJobStatusesForQrCode(
                "Bearer " + LoginPrefs.getOCTAToken(context),
                decodeJWT(context),
                SignInCompanyPrefs.getIdpType(context).toString(),
                SignInCompanyPrefs.getIdpName(context).toString(), printerId,
                decodeJWT(context)
            )
        }

        call.enqueue(object : Callback<GetJobStatusesResponse> {
            override fun onResponse(
                call: Call<GetJobStatusesResponse>,
                response: Response<GetJobStatusesResponse>
            ) {

                val getJobStatusesResponse = response.body()?.printQueueJobStatus
                if (getJobStatusesResponse?.size == 0) {
                    getdocumentListFromQrCode.clear()
                    ProgressDialog.cancelLoading()
                    runOnUiThread(Runnable {
                        dialogSelectPrinter(context);
                    })


                } else {
                    ProgressDialog.cancelLoading()
                    val parseList: List<PrintQueueJobStatusItem?>? =
                        getJobStatusesResponse
                    getdocumentListFromQrCode.clear()
                    PrintReleaseFragment.getdocumentList.clear()
                    val disposable4 = Observable.fromCallable {
                        val selectedFileList = ArrayList<SelectedFile>()
                        parseList?.forEach {
                            val selectedFile = SelectedFile()
                            selectedFile.isFromApi = true
                            selectedFile.fileName = it?.documentTitle
                            selectedFile.fileSelectedDate = it?.submittedAtRelative
                            selectedFile.filePath = it?.documentTitle.toString()
                            selectedFile.jobNum = it?.jobNumber
                            selectedFile.jobType = it?.jobType
                            selectedFile.queueId = it?.printerDeviceQueueId
                            selectedFile.userName = it?.userName
                            selectedFile.workStationId = it?.workstationId
                            selectedFile.pages = it?.pages
                            val sizeInKb: Int? = it?.jobSize
                            val size = sizeInKb?.div(1024)
                            val fileSize: String = size.toString() + "KB";
                            selectedFile.jobSize = fileSize
                            selectedFileList.add(selectedFile)
                            getdocumentListFromQrCode.add(selectedFile)
                        }
                        runOnUiThread(Runnable {
                            dialogSelectPrinter(context);
                        })

                    }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                Log.i("printer", "it=>${it}")

                            },
                            {
                                Log.i("printer", "Error=>${it.message}")
                            }
                        )
                }
            }

            override fun onFailure(call: Call<GetJobStatusesResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                // Toast.makeText(context, t.message.toString(), Toast.LENGTH_SHORT).show()
                DataDogLogger.getLogger()
                    .e("Devnco_Android printer:" + " Exception in getJobListByPrinterId:" + t.message.toString());
                Log.i("printer", t.message.toString())
            }
        })



    }

    fun decodeJWT(context: Context): String {
        var userName: String? = null
        try {
            val mapper = jacksonObjectMapper()
            val decoded: DecodedJWTResponse = mapper.readValue<DecodedJWTResponse>(
                LoginPrefs.getOCTAToken(context)?.let { JwtDecode.decoded(it) }!!
            )
            userName = decoded.user.toString()
        } catch (ex: Exception) {
            // context.toast("Failed to Decode Jwt Token")
        }
        return userName.toString()
    }

    @SuppressLint("WrongConstant")
    fun dialogSelectPrinter(context: Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_select_document)
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
         printerRecyclerView = dialog.findViewById<RecyclerView>(R.id.dialogSelectDocumentRecyclerView)
         val imgCancel = dialog.findViewById<TextView>(R.id.imgDialogSelectPrinterCancel)
         floatButton  =dialog.findViewById<FloatingActionButton>(R.id.dialogSelectPrinterFloatingButton)
        floatButton?.setBackgroundTintList(
            ContextCompat.getColorStateList(
                context,
                R.color.paleGray
            )
        )
         emptyviewForQrCode=dialog.findViewById<ConstraintLayout>(R.id.empty_viewForQrCode)

        printerRecyclerView?.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayout.VERTICAL,
                false
            )

        if(getdocumentListFromQrCode.size>0){
            emptyviewForQrCode.visibility= View.GONE
            printerRecyclerView.visibility=View.VISIBLE

        }else{
            emptyviewForQrCode.visibility= View.VISIBLE
            printerRecyclerView.visibility=View.GONE
        }

        val adapter = MyItemRecyclerViewAdapter(
            getdocumentListFromQrCode, "QrCodeScan"
        )
        printerRecyclerView?.adapter = adapter
        dialog.show()

        imgCancel.setOnClickListener {
            val intent = Intent(this@QRCodeScanActivity, MainActivity::class.java)
            startActivity(intent)
          //  Toast.makeText(this,"finish()" , Toast.LENGTH_SHORT).show()
            dialog.cancel()

        }

        floatButton?.setOnClickListener {
            //  Toast.makeText(applicationContext, "Click on float btn", Toast.LENGTH_SHORT).show()
            val printReleaseFragment = PrintReleaseFragment()
            var selectedFile: SelectedFile? = SelectedFile()

            if (BottomNavigationActivityForServerPrint.selectedServerFile.size > 0) {
                for (selectedFile in BottomNavigationActivityForServerPrint.selectedServerFile){

                //   selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile[0]
                    BottomNavigationActivityForServerPrint.selectedServerFileForQrCode.clear()
                    BottomNavigationActivityForServerPrint.selectedServerFileForQrCode.add(
                        0,
                        selectedFile
                    )

                if (selectedFile != null) {
                    if (selectedFile.jobType == "pull_print") {
                        var release_t = ""
                        if (ServerPrintRelaseFragment.selectedPrinterId != null && ServerPrintRelaseFragment.selectedPrinterToken != null) {
                            release_t =
                                ServerPrintRelaseFragment.selectedPrinterId + "," + ServerPrintRelaseFragment.selectedPrinterToken
                        }
                        printReleaseFragment.releaseJobForQrCode(context, release_t)
                        dialog.cancel()
                    } else {
                        printReleaseFragment.releaseJobForQrCode(context, "null")
                    }
                }
            }
                BottomNavigationActivityForServerPrint.selectedServerFile.clear()
        }
        }
    }


    var mMessageReceiver1: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (floatButton != null) {
                floatButton?.setBackgroundTintList(
                    ContextCompat.getColorStateList(
                        this@QRCodeScanActivity,
                        R.color.bloodOrange
                    )
                )
            }
        }
    }


    fun getdigit(string: String): String?{
        var printerId = string.filter { it.isDigit() }
        return printerId
    }

}


