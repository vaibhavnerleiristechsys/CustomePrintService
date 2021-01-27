package com.example.customeprintservice.jipp

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
import android.widget.AbsListView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.budiyev.android.codescanner.*
import com.example.customeprintservice.MainActivity
import com.example.customeprintservice.R
import com.example.customeprintservice.model.DecodedJWTResponse
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.prefs.SignInCompanyPrefs
import com.example.customeprintservice.print.MyItemRecyclerViewAdapter
import com.example.customeprintservice.print.PrintReleaseFragment
import com.example.customeprintservice.printjobstatus.model.getjobstatuses.GetJobStatusesResponse
import com.example.customeprintservice.printjobstatus.model.getjobstatuses.PrintQueueJobStatusItem
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import com.example.customeprintservice.room.SelectedFile
import com.example.customeprintservice.utils.JwtDecode
import com.example.customeprintservice.utils.ProgressDialog
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_q_r_code_scan.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QRCodeScanActivity : AppCompatActivity() {

    private lateinit var codeScanner: CodeScanner
    private val CAMERA_REQUEST_CODE=123;
    private var printerId="";
    lateinit var printerRecyclerView :RecyclerView;
    companion object {
        public val getdocumentListFromQrCode = java.util.ArrayList<SelectedFile>()
    }

    private var floatButton: FloatingActionButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_q_r_code_scan)

        val scannerView = findViewById<CodeScannerView>(R.id.scanner_view)


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
                    Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()
                    var string = it.text
                    var printerId = string.filter { it.isDigit() }
                    getJobListByPrinterId(this,printerId)


                }
            }
            codeScanner.errorCallback = ErrorCallback {
                runOnUiThread {
                    /*Toast.makeText(
                        this, "Camera initialization error: ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()*/
                }
            }

            scannerView.setOnClickListener {
                codeScanner.startPreview()
            }
        //    PrintReleaseFragment printReleaseFragment = new PrintReleaseFragment();
        //   printReleaseFragment.getJobStatusesForServerList(requireContext());
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





    fun getJobListByPrinterId(context: Context,printerId:String) {

        @SuppressLint("WrongConstant")val sh: SharedPreferences =
            context.getSharedPreferences("MySharedPref", Context.MODE_APPEND)
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")

        Log.d("IsLdap:", IsLdap!!)
        val siteId= LoginPrefs.getSiteId(context)

        var BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/pq/api/job-statuses/"

        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call = if(IsLdap.equals("LDAP")){
            apiService.getPrintJobStatusesForLdap(
                "devncoldap",
                LdapUsername.toString(),
                LdapPassword.toString()
            )
        }else{
            apiService.getPrintJobStatusesForQrCode(
                "Bearer " + LoginPrefs.getOCTAToken(context),
                decodeJWT(context),
                SignInCompanyPrefs.getIdpType(context).toString(),
                SignInCompanyPrefs.getIdpName(context).toString(),printerId,
                decodeJWT(context))
        }

        call.enqueue(object : Callback<GetJobStatusesResponse> {
            override fun onResponse(
                call: Call<GetJobStatusesResponse>,
                response: Response<GetJobStatusesResponse>
            ) {

                val getJobStatusesResponse = response.body()?.printQueueJobStatus
                if (getJobStatusesResponse?.size == 0) {
                    Toast.makeText(context, "Empty list..No Job Hold", Toast.LENGTH_SHORT)
                        .show()


                    ProgressDialog.cancelLoading()
                } else {
                    ProgressDialog.cancelLoading()
                    val parseList: List<PrintQueueJobStatusItem?>? =
                        getJobStatusesResponse
                    //  ServerPrintRelaseFragment.serverDocumentlist.clear()
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
                                //  listUpdate(it as ArrayList<SelectedFile>?, context)

                            },
                            {
                                Log.i("printer", "Error=>${it.message}")
                            }
                        )


                }

            }

            override fun onFailure(call: Call<GetJobStatusesResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Toast.makeText(context, t.message.toString(), Toast.LENGTH_SHORT).show()
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
    fun dialogSelectPrinter(context:Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_select_document)
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

         printerRecyclerView =
            dialog.findViewById<RecyclerView>(R.id.dialogSelectDocumentRecyclerView)
        val imgCancel = dialog.findViewById<ImageView>(R.id.imgDialogSelectPrinterCancel)

         floatButton  =dialog.findViewById<FloatingActionButton>(R.id.dialogSelectPrinterFloatingButton)

        printerRecyclerView?.layoutManager =
            LinearLayoutManager(
                applicationContext,
                LinearLayout.VERTICAL,
                false
            )
        val adapter = MyItemRecyclerViewAdapter(
            getdocumentListFromQrCode
        )
        printerRecyclerView?.adapter = adapter
        dialog.show()

        imgCancel.setOnClickListener {
            val intent = Intent(this@QRCodeScanActivity, QRCodeScanActivity::class.java)
            startActivity(intent)
        }

        floatButton?.setOnClickListener {
            Toast.makeText(applicationContext, "Click on float btn", Toast.LENGTH_SHORT).show()
            val printReleaseFragment = PrintReleaseFragment()
            printReleaseFragment.releaseJob(context)
            getJobListByPrinterId(this,printerId)

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
}


