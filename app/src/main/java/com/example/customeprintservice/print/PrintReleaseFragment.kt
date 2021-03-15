package com.example.customeprintservice.print

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.customeprintservice.MainActivity
import com.example.customeprintservice.PrintService
import com.example.customeprintservice.R
import com.example.customeprintservice.adapter.FragmentSelectedFileListAdapter
import com.example.customeprintservice.jipp.FileUtils
import com.example.customeprintservice.jipp.PrinterDiscoveryActivity
import com.example.customeprintservice.jipp.PrinterModel
import com.example.customeprintservice.model.DecodedJWTResponse
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.prefs.SignInCompanyPrefs
import com.example.customeprintservice.printjobstatus.model.canceljob.CancelJobRequest
import com.example.customeprintservice.printjobstatus.model.canceljob.CancelJobResponse
import com.example.customeprintservice.printjobstatus.model.canceljob.DeleteJobsItem
import com.example.customeprintservice.printjobstatus.model.getjobstatuses.GetJobStatusesResponse
import com.example.customeprintservice.printjobstatus.model.getjobstatuses.PrintQueueJobStatusItem
import com.example.customeprintservice.printjobstatus.model.releasejob.ReleaseJobRequest
import com.example.customeprintservice.printjobstatus.model.releasejob.ReleaseJobResponse
import com.example.customeprintservice.printjobstatus.model.releasejob.ReleaseJobsItem
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import com.example.customeprintservice.room.SelectedFile
import com.example.customeprintservice.utils.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_print_release.*
import kotlinx.android.synthetic.main.fragment_printers.*
import okhttp3.ResponseBody
import org.apache.commons.codec.binary.Base64;
import org.jetbrains.anko.doAsync
import org.slf4j.LoggerFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class PrintReleaseFragment : Fragment() {

    private var permissionsHelper: PermissionHelper? = null
    private val bundle = Bundle()
    private var isFileSelected: Boolean = false
    private var list = ArrayList<SelectedFile>()
    private lateinit var app: PrintService
    private var adapter: FragmentSelectedFileListAdapter? = null
    private var toolbar: Toolbar? = null
    private var textToolbar: TextView? = null
    private var backButton: ImageButton? = null
    private val compositeDisposable = CompositeDisposable()
    private val releaseJobRequest = ReleaseJobRequest()
    private val releaseJobCheckedList = ArrayList<SelectedFile>()
    private var releaseJobCheckedListForServer = ArrayList<SelectedFile>()
    private val swipeContainer: SwipeRefreshLayout? = null
    var logger = LoggerFactory.getLogger(PrintReleaseFragment::class.java)

    companion object {
        public val getdocumentList = java.util.ArrayList<SelectedFile>()
    }

    var selectedServerFilelist = ArrayList<SelectedFile>()
     var localdocumentFromsharedPrefences = ArrayList<SelectedFile>()
    @SuppressLint("CheckResult")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initConfig()
        releaseJobCheckedList.clear()
        val disposable = Observable.fromCallable {
            val list = this.arguments?.getSerializable("sharedFileList") as ArrayList<SelectedFile>?
            if (list?.size!! > 0) {
                app.dbInstance().selectedFileDao().save(list)
            }
            list.clear()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                Log.i("printer", "Saved")
                logger.info("printer"+"Saved")
            }, {
                it.message
            })
        compositeDisposable.add(disposable)

        return inflater.inflate(R.layout.fragment_print_release, container, false)
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermissions()

        ProgressDialog.showLoadingDialog(requireContext(), "Getting Hold jobs")
        getJobStatuses(
            requireContext(),
            decodeJWT(),
            SignInCompanyPrefs.getIdpType(requireContext()).toString(),
            SignInCompanyPrefs.getIdpName(
                requireContext()
            ).toString()
        )

        drawer.setOnClickListener {
               val intent = Intent(context, MainActivity::class.java)
             startActivity(intent)
        }
        serverPrintScreen.setOnClickListener {
            val intent = Intent(context, BottomNavigationActivityForServerPrint::class.java)
            startActivity(intent)
        }
       // val printersFragment =PrintersFragment()
       // printersFragment.getPrinterList(requireContext(),decodeJWT());

        btnFragmentSelectDoc.setOnClickListener {
            if (Permissions().checkAndRequestPermissions(context as Activity)) {
                val i = Intent(
                    Intent.ACTION_GET_CONTENT,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                i.type = "*/*"
                startActivityForResult(i, 1)

            } else {
                Toast.makeText(context as Activity, "Please accept Permissions", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        btnFragmentPrintReleaseNext.setOnClickListener {
            if (isFileSelected) {
                val intent = Intent(context, PrinterDiscoveryActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            } else {
                Toast.makeText(context, "Select the Document", Toast.LENGTH_SHORT).show()
            }

        }

        val disposable2 = Observable.fromCallable {
            app.dbInstance().selectedFileDao().loadAll()
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                if (it.isNotEmpty()) {
                    isFileSelected = true
                    bundle.putSerializable("selectedFileList", it as ArrayList<SelectedFile>)
                }
                listUpdate(it as ArrayList<SelectedFile>?, requireContext())
            }
        compositeDisposable.add(disposable2)
    }

    fun cancelJob(context: Context) {
        @SuppressLint("WrongConstant")val sh: SharedPreferences =
            context.getSharedPreferences("MySharedPref", Context.MODE_APPEND)
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")


        ProgressDialog.showLoadingDialog(context, "Delete Job")
        releaseJobCheckedListForServer = BottomNavigationActivityForServerPrint.selectedServerFile as ArrayList<SelectedFile>
        val siteId= LoginPrefs.getSiteId(context)
        var BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/pq/api/job-statuses/cancel/"


        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val jobStatusCancel = CancelJobRequest()
        val deleteJobs = ArrayList<DeleteJobsItem>()
        releaseJobCheckedListForServer.forEach {
            val deleteJobsItem = DeleteJobsItem()
            deleteJobsItem.jobNum = it.jobNum
            if(it.jobType.toString().equals("secure_release")) {
                deleteJobsItem.jobType = "1"
            }else{
                deleteJobsItem.jobType = "2"
            }
            deleteJobsItem.queueId = it.queueId
            deleteJobsItem.userName = it.userName
            deleteJobsItem.workstationId = it.workStationId
            deleteJobs.add(deleteJobsItem)
        }
        jobStatusCancel.deleteJobs = deleteJobs


        val call = if(IsLdap.equals("LDAP")){
            apiService.jobStatusCancelForLdap(
                siteId.toString(),
                LdapUsername.toString(),
                LdapPassword.toString(),
                jobStatusCancel
            )
        }else if(siteId.toString().contains("google")){
            apiService.jobStatusCancelForGoogle(
                "Bearer " + LoginPrefs.getOCTAToken(context),
                decodeJWT(context),
                SignInCompanyPrefs.getIdpType(context).toString(),
                SignInCompanyPrefs.getIdpName(context).toString(),
                jobStatusCancel,
                "serverId"
            )
        }
        else{
            apiService.jobStatusCancel(
                "Bearer " + LoginPrefs.getOCTAToken(context),
                decodeJWT(context),
                SignInCompanyPrefs.getIdpType(context).toString(),
                SignInCompanyPrefs.getIdpName(context).toString(),
                jobStatusCancel
            )
        }



        call.enqueue(object : Callback<CancelJobResponse> {
            override fun onResponse(
                call: Call<CancelJobResponse>,
                response: Response<CancelJobResponse>
            ) {
                ProgressDialog.cancelLoading()
                if (response.code() == 200) {
                    BottomNavigationActivityForServerPrint.selectedServerFile.clear()
                    val resp = response.body().toString()
                    Log.i("printer", "response cancel job==>${resp}")
                    logger.info("printer"+ "response cancel job==>${resp}")
                    ProgressDialog.showLoadingDialog(context, "Refreshing Job List")
                    getJobStatuses(
                        context,
                        decodeJWT(context),
                        SignInCompanyPrefs.getIdpType(context).toString(),
                        SignInCompanyPrefs.getIdpName(context).toString()
                    )

                }
            }

            override fun onFailure(call: Call<CancelJobResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Toast.makeText(context, "Validation Failed", Toast.LENGTH_SHORT).show()
                Log.i("printer", "Error response cancel job==>${t.message}")
                logger.info("printer"+ "Error response cancel job==>${t.message}")
            }
        })
    }

    fun releaseJob(context: Context, release_t: String){
        @SuppressLint("WrongConstant")val sh: SharedPreferences = context.getSharedPreferences(
            "MySharedPref",
            Context.MODE_APPEND
        )
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")

        Log.d("IsLdap:", IsLdap!!)
        logger.info("IsLdap:"+ IsLdap!!)

        ProgressDialog.showLoadingDialog(context, "Released Job")
        releaseJobCheckedListForServer = BottomNavigationActivityForServerPrint.selectedServerFile as ArrayList<SelectedFile>
        val siteId= LoginPrefs.getSiteId(context)
        var BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/pq/api/job-statuses/release/"

        val apiService = RetrofitClient(context).getRetrofitInstance(BASE_URL).create(ApiService::class.java)

        val releaseJobRequest = ReleaseJobRequest()
        val releaseJobs = ArrayList<ReleaseJobsItem>()
        releaseJobCheckedListForServer.forEach {
            val releaseJobsItem = ReleaseJobsItem()
            releaseJobsItem.jobNum = it.jobNum
            Log.d("jobtype", it.jobType.toString())
            logger.info("jobtype"+ it.jobType.toString())
            if(it.jobType.toString().equals("secure_release")) {
                releaseJobsItem.jobType = "1"
            }else{
                releaseJobsItem.jobType = "2"
            }
            releaseJobsItem.queueId = it.queueId
            releaseJobsItem.userName = it.userName
            releaseJobsItem.workstationId = it.workStationId
            releaseJobs.add(releaseJobsItem)
        }
        releaseJobRequest.releaseJobs = releaseJobs

        val call = if(IsLdap.equals("LDAP")){
            if(!release_t.equals("null")){
            apiService.releaseJobForPullPrinterLdap(
                releaseJobRequest,
                siteId.toString(),
                LdapUsername.toString(),
                LdapPassword.toString(),
                "json:api",
                release_t
            )
            }
            else{
                apiService.releaseJobForLdap(
                    releaseJobRequest,
                    siteId.toString(),
                    LdapUsername.toString(),
                    LdapPassword.toString()
                )
            }

        }else if(siteId.toString().contains("google")){
            if(!release_t.equals("null")) {
                apiService.releaseJobForPullPrinterForGoogle(
                    releaseJobRequest, "Bearer " + LoginPrefs.getOCTAToken(context),
                    decodeJWT(context),
                    SignInCompanyPrefs.getIdpType(context).toString(),
                    SignInCompanyPrefs.getIdpName(context).toString(),
                    "json:api",
                    release_t,
                    "serverId"
                )
            }else{
                apiService.releaseJobForGoogle(
                    releaseJobRequest, "Bearer " + LoginPrefs.getOCTAToken(context),
                    decodeJWT(context),
                    SignInCompanyPrefs.getIdpType(context).toString(),
                    SignInCompanyPrefs.getIdpName(context).toString(),
                    "serverId"
                )
            }
        }
        else{
            if(!release_t.equals("null")) {
                apiService.releaseJobForPullPrinter(
                    releaseJobRequest, "Bearer " + LoginPrefs.getOCTAToken(context),
                    decodeJWT(context),
                    SignInCompanyPrefs.getIdpType(context).toString(),
                    SignInCompanyPrefs.getIdpName(context).toString(),
                    "json:api",
                    release_t
                )
            }else{
                apiService.releaseJob(
                    releaseJobRequest, "Bearer " + LoginPrefs.getOCTAToken(context),
                    decodeJWT(context),
                    SignInCompanyPrefs.getIdpType(context).toString(),
                    SignInCompanyPrefs.getIdpName(context).toString()
                )
            }
        }


        call.enqueue(object : Callback<ReleaseJobResponse> {
            override fun onResponse(
                call: Call<ReleaseJobResponse>,
                response: Response<ReleaseJobResponse>
            ) {
                ProgressDialog.cancelLoading()
                if (response.code() == 200) {
                    val response = response.body().toString()
                    Log.i("printer", "response release job==>${response}")
                    logger.info("printer"+ "response release job==>${response}")
                    //BottomNavigationActivityForServerPrint.selectedServerFile.clear()
                    val activity: Activity? = activity
                    if (activity != null) {

                    }
                    sendMetaData(context)
                    ProgressDialog.showLoadingDialog(context, "Refreshing Job List")
                    getJobStatuses(
                        context,
                        decodeJWT(context),
                        SignInCompanyPrefs.getIdpType(context).toString(),
                        SignInCompanyPrefs.getIdpName(context).toString()
                    )

                    dialogSuccessfullyPrint(context);


                }
            }

            override fun onFailure(call: Call<ReleaseJobResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Toast.makeText(context, "Validation Failed", Toast.LENGTH_SHORT).show()
                Log.i("printer", "Error response release job==>${t.message}")
                logger.info("printer"+ "Error response release job==>${t.message}")
            }

        })
    }



    fun getJobStatuses(context: Context, userName: String, idpType: String, idpName: String) {
        @SuppressLint("WrongConstant")val sh: SharedPreferences = context.getSharedPreferences(
            "MySharedPref",
            Context.MODE_APPEND
        )
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")

        val siteId= LoginPrefs.getSiteId(context)
        var BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/pq/api/job-statuses/"

        val apiService = RetrofitClient(context).getRetrofitInstance(BASE_URL).create(ApiService::class.java)

        val call = if(IsLdap.equals("LDAP")){
         apiService.getPrintJobStatusesForLdap(
             siteId.toString(),
             LdapUsername.toString(),
             LdapPassword.toString()
         )
        }else if(siteId.toString().contains("google")){
            apiService.getPrintJobStatusesForGoogle(
                "Bearer " + LoginPrefs.getOCTAToken(context),
                userName,
                idpType,
                idpName,
                "serverId", userName, "printerDeviceQueue.printers"
            )
        }
        else{
            apiService.getPrintJobStatuses(
                "Bearer " + LoginPrefs.getOCTAToken(context),
                userName,
                idpType,
                idpName, userName, "printerDeviceQueue.printers"
            )
       }

        call.enqueue(object : Callback<GetJobStatusesResponse> {
            override fun onResponse(
                call: Call<GetJobStatusesResponse>,
                response: Response<GetJobStatusesResponse>
            ) {

                val getJobStatusesResponse = response.body()?.printQueueJobStatus
                if (getJobStatusesResponse?.size == 0) {
                    getdocumentList.clear()
                    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                        context
                    )
                    val gson = Gson()
                    val jsonText: String? = prefs.getString("localdocumentlist", null)
                    val type: Type =
                        object : TypeToken<java.util.ArrayList<SelectedFile?>?>() {}.getType()
                    if (jsonText != null) {
                        localdocumentFromsharedPrefences = gson.fromJson(jsonText, type)
                        getdocumentList.addAll(localdocumentFromsharedPrefences)
                    }
                    ProgressDialog.cancelLoading()
                } else {
                    ProgressDialog.cancelLoading()
                    val parseList: List<PrintQueueJobStatusItem?>? = getJobStatusesResponse
                    val disposable4 = Observable.fromCallable {
                        val selectedFileList = ArrayList<SelectedFile>()
                        getdocumentList.clear()
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
                            selectedFile.printerId = it?.printerDeviceQueue?.printers?.get(0)?.id
                            selectedFileList.add(selectedFile)
                            getdocumentList.add(selectedFile)
                        }
                        val prefs: SharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(
                                context
                            )
                        val gson = Gson()
                        val jsonText: String? = prefs.getString("localdocumentlist", null)
                        val type: Type =
                            object : TypeToken<java.util.ArrayList<SelectedFile?>?>() {}.getType()
                        localdocumentFromsharedPrefences = gson.fromJson(jsonText, type)
                        getdocumentList.addAll(localdocumentFromsharedPrefences)

                        val intent = Intent("refreshdocumentadapter")
                        intent.putExtra("refreshdocumentadapter", "refresh")
                        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent)


                        app.dbInstance().selectedFileDao().deleteItemsFromApi()
                        app.dbInstance().selectedFileDao().save(selectedFileList)
                        app.dbInstance().selectedFileDao().loadAll()
                    }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                Log.i("printer", "it=>${it}")
                                logger.info("printer"+ "it=>${it}")
                                listUpdate(it as ArrayList<SelectedFile>?, requireContext())
                            },
                            {
                                Log.i("printer", "Error=>${it.message}")
                                logger.info("printer"+ "Error=>${it.message}")
                            }
                        )
                    compositeDisposable.add(disposable4)
                    isFileSelected = true
                    Log.i("printer", "list of Files-->$list")
                    logger.info("printer"+ "list of Files-->$list")

                }

            }

            override fun onFailure(call: Call<GetJobStatusesResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_SHORT).show()
                Log.i("printer", t.message.toString())
                logger.info("printer"+ t.message.toString())
            }
        })
    }

    private fun checkPermissions() {
        permissionsHelper = PermissionHelper()
        permissionsHelper!!.checkAndRequestPermissions(
            context as Activity,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsHelper!!.onRequestPermissionsResult(
            context as Activity,
            requestCode,
            permissions,
            grantResults
        )
    }


    @SuppressLint("SimpleDateFormat", "CheckResult")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == AppCompatActivity.RESULT_OK) {
            val uri: Uri = data?.data!!
            val realPath = FileUtils.getPath(context as Activity, uri)
            val file: File = File(realPath)

            val disposable3 = Observable.fromCallable {
                val saveList = ArrayList<SelectedFile>()
                val selectedFile = SelectedFile()
                selectedFile.apply {
                    fileName = file.name
                    filePath = realPath
                    isFromApi = false
                    fileSelectedDate = SimpleDateFormat("yyyy-MM-dd HH:mm").format(Date())
                    saveList.add(this)
                }
                app.dbInstance().selectedFileDao().save(saveList)
                app.dbInstance().selectedFileDao().loadAll()
            }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        Log.i("printer", "it=>${it}")
                        logger.info("printer"+ "it=>${it}")
                        isFileSelected = true
                        bundle.putSerializable("selectedFileList", it as ArrayList<SelectedFile>)
                        listUpdate(it as ArrayList<SelectedFile>?, requireContext())
                    },
                    {
                        Log.i("printer", "Error=>${it.message}")
                        logger.info("printer"+ "Error=>${it.message}")
                    }
                )
            compositeDisposable.add(disposable3)
            isFileSelected = true
            Log.i("printer", "list of Files-->$list")
            logger.info("printer"+ "list of Files-->$list")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }


    private fun initConfig() {
        app = activity?.application as PrintService
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
            logger.info("exception"+ ex.toString())
        }
        return userName.toString()
    }

    private fun validateToken() {
//        Log.i("Printer", "IdpUrl->${SignInCompanyPrefs.getIdpUrl(requireContext()).toString()}")

        val apiService = RetrofitClient(requireContext())
            .getRetrofitInstance(
                "${SignInCompanyPrefs.getIdpUrl(requireContext()).toString()}/validate-token/"
            )
            .create(ApiService::class.java)
        val call = apiService.validateToken(
            LoginPrefs.getOCTAToken(requireContext()),
            decodeJWT()
        )

        call?.enqueue(object : Callback<Any> {
            override fun onResponse(call: Call<Any>, response: Response<Any>) {
                if (response.code() == 204)
                    Log.i("printer", "response validate token=>${response.isSuccessful}")
                logger.info("printer"+ "response validate token=>${response.isSuccessful}")
                Log.i("printer", "response validate token=>${response}")
                logger.info("printer"+ "response validate token=>${response}")
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Log.i("printer", "response validate token Error=>${t.message}")
                logger.info("printer"+ "response validate token Error=>${t.message}")
            }
        })
    }

    @SuppressLint("WrongConstant")
    private fun listUpdate(list: ArrayList<SelectedFile>?, context: Context) {
        val recyclerViewDocumentList =
            view?.findViewById<RecyclerView>(R.id.recyclerViewDocumentList)

        recyclerViewDocumentList?.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayout.VERTICAL,
                false
            )

        adapter = FragmentSelectedFileListAdapter(
            context as Activity,
            list
        )
        adapter?.itemClick()?.doOnNext {
            Log.i("printer", "item checked ===>${it}")
            logger.info("printer"+ "item checked ===>${it}")
            releaseJobCheckedList.add(it)
        }?.subscribe()
        recyclerViewDocumentList?.adapter = adapter
    }


    fun getJobStatusesForServerList(context: Context) {

        @SuppressLint("WrongConstant")val sh: SharedPreferences =
            context.getSharedPreferences("MySharedPref", Context.MODE_APPEND)
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")

        Log.d("IsLdap:", IsLdap!!)

        val siteId= LoginPrefs.getSiteId(context)

        var  BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/pq/api/job-statuses/"

        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call = if(IsLdap.equals("LDAP")){
            apiService.getPrintJobStatusesForLdap(
                siteId.toString(),
                LdapUsername.toString(),
                LdapPassword.toString()
            )
        }else if(siteId.toString().contains("google")){
            apiService.getPrintJobStatusesForGoogle(
                "Bearer " + LoginPrefs.getOCTAToken(context),
                decodeJWT(context),
                SignInCompanyPrefs.getIdpType(context).toString(),
                SignInCompanyPrefs.getIdpName(context).toString(),
                "serverId",
                decodeJWT(context), "printerDeviceQueue.printers"
            )
        }
        else{
            apiService.getPrintJobStatuses(
                "Bearer " + LoginPrefs.getOCTAToken(context),
                decodeJWT(context),
                SignInCompanyPrefs.getIdpType(context).toString(),
                SignInCompanyPrefs.getIdpName(context).toString(),
                decodeJWT(context), "printerDeviceQueue.printers"
            )
        }

        call.enqueue(object : Callback<GetJobStatusesResponse> {
            override fun onResponse(
                call: Call<GetJobStatusesResponse>,
                response: Response<GetJobStatusesResponse>
            ) {

                val getJobStatusesResponse = response.body()?.printQueueJobStatus
                if (getJobStatusesResponse?.size == 0) {
                    doAsync {
                        app.dbInstance().selectedFileDao().deleteItemsFromApi()
                    }
                    // ServerPrintRelaseFragment.serverDocumentlist.clear()
                    getdocumentList.clear();
                    Toast.makeText(context, "Empty list..No Job Hold", Toast.LENGTH_SHORT)
                        .show()

                    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(
                        context
                    )
                    val gson = Gson()
                    val jsonText: String? = prefs.getString("localdocumentlist", null)
                    val type: Type =
                        object : TypeToken<java.util.ArrayList<SelectedFile?>?>() {}.getType()
                    localdocumentFromsharedPrefences = gson.fromJson(jsonText, type)

                    //    ServerPrintRelaseFragment.serverDocumentlist.addAll(localdocumentFromsharedPrefences);
                    getdocumentList.addAll(localdocumentFromsharedPrefences)

                    val intent = Intent("refreshdocumentadapter")
                    intent.putExtra("refreshdocumentadapter", "refresh")
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

                    ProgressDialog.cancelLoading()
                } else {
                    ProgressDialog.cancelLoading()
                    val parseList: List<PrintQueueJobStatusItem?>? =
                        getJobStatusesResponse
                    //  ServerPrintRelaseFragment.serverDocumentlist.clear()
                    getdocumentList.clear()
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
                            // ServerPrintRelaseFragment.serverDocumentlist.add(selectedFile)
                            getdocumentList.add(selectedFile)
                        }
                        val prefs: SharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(
                                context
                            )
                        val gson = Gson()
                        val jsonText: String? = prefs.getString("localdocumentlist", null)
                        val type: Type =
                            object : TypeToken<java.util.ArrayList<SelectedFile?>?>() {}.getType()
                        localdocumentFromsharedPrefences = gson.fromJson(jsonText, type)

                        // ServerPrintRelaseFragment.serverDocumentlist.addAll(localdocumentFromsharedPrefences);
                        getdocumentList.addAll(localdocumentFromsharedPrefences)
                        // ServerPrintRelaseFragment.serverDocumentlist.addAll(MainActivity.list);


                        app.dbInstance().selectedFileDao().deleteItemsFromApi()
                        app.dbInstance().selectedFileDao().save(selectedFileList)
                        app.dbInstance().selectedFileDao().loadAll()
                    }.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            {
                                Log.i("printer", "it=>${it}")
                                logger.info("printer"+ "it=>${it}")
                                //  listUpdate(it as ArrayList<SelectedFile>?, context)
                            },
                            {
                                Log.i("printer", "Error=>${it.message}")
                                logger.info("printer"+ "Error=>${it.message}")
                            }
                        )
                    compositeDisposable.add(disposable4)
                    isFileSelected = true
                    Log.i("printer", "list of Files-->$list")
                    logger.info("printer"+ "list of Files-->$list")

                }

            }

            override fun onFailure(call: Call<GetJobStatusesResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_SHORT).show()
                Log.i("printer", t.message.toString())
                logger.info("printer"+ t.message.toString())
            }
        })



    }

    @SuppressLint("WrongConstant")
    fun dialogSuccessfullyPrint(context: Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_successful_print)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        val window = dialog.window
        window!!.setLayout(
            AbsListView.LayoutParams.WRAP_CONTENT,
            AbsListView.LayoutParams.WRAP_CONTENT
        )
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        val wlp = window.attributes
        wlp.gravity = Gravity.CENTER
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(0.5f)
        window.attributes = wlp
        val button =
            dialog.findViewById<Button>(R.id.ok)
        dialog.show()


        button.setOnClickListener {
            dialog.cancel()
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)

        }
    }


//************************************************************
fun sendMetaData(context: Context){
    var username =decodeJWT(context)
    var printerName:String=""
    var selectedPrinteripAddress:String=""
    var printerId:String=""
    var fileName:String=""
    if (BottomNavigationActivityForServerPrint.selectedServerFile.size > 0) {
       val selectedFile:SelectedFile = BottomNavigationActivityForServerPrint.selectedServerFile[0]
        fileName=selectedFile.fileName.toString()
    }
    if(BottomNavigationActivityForServerPrint.selectedPrinter!=null){
         val selectedPrinter:PrinterModel = BottomNavigationActivityForServerPrint.selectedPrinter
        selectedPrinteripAddress =selectedPrinter.printerHost.toString().replace("/", "")
         printerName =selectedPrinter.serviceName.toString()
        printerId=selectedPrinter.id.toString()
    }

    val ipAddress = IpAddress.getLocalIpAddress();
    if(ipAddress!=null) {
        Log.d("ipAddress of device:", ipAddress);
        logger.info("ipAddress of device:"+ ipAddress);
    }
    var BASE_URL =""
    val companyUrl = LoginPrefs.getCompanyUrl(context)
    val siteId= LoginPrefs.getSiteId(context)
    val xIdpType =SignInCompanyPrefs.getIdpType(context)
    val xIdpName =SignInCompanyPrefs.getIdpName(context)

    val idpInfo ="{\"os\":"+"android"+",\"idpName\":"+xIdpName+",\"username\":"+username+",\"isLoggedIn\":"+true+",\"type\":"+xIdpType+",\"token\":"+LoginPrefs.getOCTAToken(context)+"}";
    val bytesEncoded: ByteArray = Base64.encodeBase64(idpInfo.toByteArray())
    val encodedIdpInfo =String(bytesEncoded)
    Log.d("encodedIdpInfo", URLEncoder.encode(idpInfo));

    BASE_URL = "https://"+companyUrl+"/client/gateway.php/"

    val apiService = RetrofitClient(context).getRetrofitInstance(BASE_URL).create(ApiService::class.java)

    val call = if(siteId.toString().contains("google")){
        apiService.sendMetaDataForGoogle(
            siteId.toString(),
            "Bearer ${LoginPrefs.getOCTAToken(context)}",
            username,
            xIdpType.toString(),
            xIdpName.toString(),
            "1",
            " <?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                    " <printjobs>\n" +
                    " <machine>\n" +
                    " <commonnames>\n" +
                    "<name>"+ipAddress+"</name>\n" +
                    "</commonnames>\n" +
                    "</machine>\n" +
                    "<jobs>\n" +
                    "<job iscolor=\"0\" istcpip=\"1\">\n" +
                    " <printer id=\""+printerId+"\">\n" +
                    "<name>"+printerName+"</name>\n" +
                    "<share />\n" +
                    "</printer>\n" +
                    "<source>\n" +
                    "<user>"+username+"</user>\n" +
                    "<machine>"+ipAddress+"</machine>\n" +
                    "<ip_address>"+ipAddress+"</ip_address>\n" +
                    "<mgr />\n" +
                    "<dpt />\n" +
                    "<com />\n" +
                    "<fn />\n" +
                    " <jt />\n" +
                    "<aun>"+URLEncoder.encode(idpInfo)+"</aun>\n" +
                    "</source>\n" +
                    "<document pl=\"2794\" pw=\"2159\" duplex=\"2\" length=\"1\">\n" +
                    "<submitted>2021-03-11 04:31:54</submitted>\n" +
                    "<completed>2021-03-11 04:32:14</completed>\n" +
                    "<title>"+fileName+"</title>\n" +
                    " </document>\n" +
                    "</job>\n" +
                    " </jobs>\n" +
                    "</printjobs>"

        )
    }else{
        apiService.sendMetaDataForOtherIdp(
            siteId.toString(),
            "Bearer ${LoginPrefs.getOCTAToken(context)}",
            username,
            xIdpType.toString(),
            xIdpName.toString(),
            "1",
           " <?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
       " <printjobs>\n" +
       " <machine>\n" +
       " <commonnames>\n" +
       "<name>"+ipAddress+"</name>\n" +
        "</commonnames>\n" +
        "</machine>\n" +
       "<jobs>\n" +
        "<job iscolor=\"0\" istcpip=\"1\">\n" +
       " <printer id=\""+printerId+"\">\n" +
        "<name>"+printerName+"</name>\n" +
        "<share />\n" +
        "</printer>\n" +
        "<source>\n" +
        "<user>"+username+"</user>\n" +
        "<machine>"+ipAddress+"</machine>\n" +
        "<ip_address>"+ipAddress+"</ip_address>\n" +
        "<mgr />\n" +
       "<dpt />\n" +
        "<com />\n" +
       "<fn />\n" +
        " <jt />\n" +
       "<aun>"+URLEncoder.encode(idpInfo)+"</aun>\n" +
       "</source>\n" +
       "<document pl=\"2794\" pw=\"2159\" duplex=\"2\" length=\"1\">\n" +
       "<submitted>2021-03-11 04:31:54</submitted>\n" +
       "<completed>2021-03-11 04:32:14</completed>\n" +
       "<title>"+fileName+"</title>\n" +
       " </document>\n" +
       "</job>\n" +
       " </jobs>\n" +
       "</printjobs>"
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
                } catch (e: Exception) {
                    Log.i("printer", "e=>${e.message.toString()}")
                    logger.info("printer"+ "e=>${e.message.toString()}")
                }
            } else {
                ProgressDialog.cancelLoading()

            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            ProgressDialog.cancelLoading()

            Log.i("printer", "Error html response==>${t.message.toString()}")
            logger.info("printer"+ "Error html response==>${t.message.toString()}")
        }
    })
}


}

//https://www.youtube.com/watch?v=vPLKNsQEAEc
//https://www.youtube.com/watch?v=nC9E9dvw2eY
