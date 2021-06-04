package com.example.customeprintservice.print

//import org.slf4j.LoggerFactory
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
import android.os.Handler
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
import com.example.customeprintservice.model.ServerJobsModel
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.prefs.LoginPrefs.Companion.getTenantUrl
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
import com.example.customeprintservice.utils.ProgressDialog.Companion.cancelLoading
import com.example.customeprintservice.utils.ProgressDialog.Companion.showLoadingDialog
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_print_release.*
import okhttp3.ResponseBody
import org.apache.commons.codec.binary.Base64
import org.jetbrains.anko.doAsync
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.reflect.Type
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

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
  //  var logger = LoggerFactory.getLogger(PrintReleaseFragment::class.java)

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
                DataDogLogger.getLogger().i("Devnco_Android printer" + "Saved")
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
        val tanentUrl = getTenantUrl(context)
        var BASE_URL = ""+tanentUrl+"/"+siteId+"/pq/api/job-statuses/cancel/"
       // var BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/pq/api/job-statuses/cancel/"


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
                    DataDogLogger.getLogger()
                        .i("Devnco_Android printer" + "response cancel job==>${resp}")
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
                DataDogLogger.getLogger()
                    .i("Devnco_Android printer" + "Error response cancel job==>${t.message}")
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
        var pageCount:Int=1
        Log.d("IsLdap:", IsLdap!!)
        DataDogLogger.getLogger().i("Devnco_Android IsLdap:" + IsLdap!!)

        ProgressDialog.showLoadingDialog(context, "Released Job")
        releaseJobCheckedListForServer = BottomNavigationActivityForServerPrint.selectedServerFile as ArrayList<SelectedFile>
        val siteId= LoginPrefs.getSiteId(context)
        val tanentUrl = getTenantUrl(context)
        var BASE_URL = ""+tanentUrl+"/"+siteId+"/pq/api/job-statuses/release/"
      //  var BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/pq/api/job-statuses/release/"

        val apiService = RetrofitClient(context).getRetrofitInstance(BASE_URL).create(ApiService::class.java)

        val releaseJobRequest = ReleaseJobRequest()
        val releaseJobs = ArrayList<ReleaseJobsItem>()
        releaseJobCheckedListForServer.forEach {
            val releaseJobsItem = ReleaseJobsItem()
            releaseJobsItem.jobNum = it.jobNum
            Log.d("jobtype", it.jobType.toString())
            DataDogLogger.getLogger().i("Devnco_Android jobtype" + it.jobType.toString())
            if(it.jobType.toString().equals("secure_release")) {
                releaseJobsItem.jobType = "1"
            }else{
                releaseJobsItem.jobType = "2"
            }
            releaseJobsItem.queueId = it.queueId
            releaseJobsItem.userName = it.userName
            releaseJobsItem.workstationId = it.workStationId
            pageCount= it.pages!!
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
                    DataDogLogger.getLogger()
                        .i("Devnco_Android printer" + "response release job==>${response}")
                    //BottomNavigationActivityForServerPrint.selectedServerFile.clear()
                    val activity: Activity? = activity
                    if (activity != null) {

                    }
                    //   sendMetaData(context,pageCount)
                    //  sendMetaData(context,1)
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
                DataDogLogger.getLogger()
                    .i("Devnco_Android printer" + "Error response release job==>${t.message}")
            }

        })
    }

    //****************************************release job For qrCode***********************************************************
    fun releaseJobForQrCode(context: Context, release_t: String){
        @SuppressLint("WrongConstant")val sh: SharedPreferences = context.getSharedPreferences(
            "MySharedPref",
            Context.MODE_APPEND
        )
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")
        var pageCount:Int=1
        Log.d("IsLdap:", IsLdap!!)
        DataDogLogger.getLogger().i("Devnco_Android IsLdap:" + IsLdap!!)

        ProgressDialog.showLoadingDialog(context, "Released Job")


            releaseJobCheckedListForServer =
                BottomNavigationActivityForServerPrint.selectedServerFileForQrCode as ArrayList<SelectedFile>

        val siteId= LoginPrefs.getSiteId(context)
        val tanentUrl = getTenantUrl(context)
        var BASE_URL = ""+tanentUrl+"/"+siteId+"/pq/api/job-statuses/release/"
        //  var BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/pq/api/job-statuses/release/"

        val apiService = RetrofitClient(context).getRetrofitInstance(BASE_URL).create(ApiService::class.java)

        val releaseJobRequest = ReleaseJobRequest()
        val releaseJobs = ArrayList<ReleaseJobsItem>()
        releaseJobCheckedListForServer.forEach {
            val releaseJobsItem = ReleaseJobsItem()
            releaseJobsItem.jobNum = it.jobNum
            Log.d("jobtype", it.jobType.toString())
            DataDogLogger.getLogger().i("Devnco_Android jobtype" + it.jobType.toString())
            if(it.jobType.toString().equals("secure_release")) {
                releaseJobsItem.jobType = "1"
            }else{
                releaseJobsItem.jobType = "2"
            }
            releaseJobsItem.queueId = it.queueId
            releaseJobsItem.userName = it.userName
            releaseJobsItem.workstationId = it.workStationId
            pageCount= it.pages!!
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
                    DataDogLogger.getLogger()
                        .i("Devnco_Android printer" + "response release job==>${response}")
                    //BottomNavigationActivityForServerPrint.selectedServerFile.clear()
                    val activity: Activity? = activity
                    if (activity != null) {

                    }
                    //   sendMetaData(context,pageCount)
                    //  sendMetaData(context,1)
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
                DataDogLogger.getLogger()
                    .i("Devnco_Android printer" + "Error response release job==>${t.message}")
            }

        })
    }
    //*******************************************release job for qrCode end*********************************************************



    fun getJobStatuses(context: Context, userName: String, idpType: String, idpName: String) {
        @SuppressLint("WrongConstant")val sh: SharedPreferences = context.getSharedPreferences(
            "MySharedPref",
            Context.MODE_APPEND
        )
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")

        val siteId= LoginPrefs.getSiteId(context)
        val tanentUrl = getTenantUrl(context)
        var BASE_URL = ""+tanentUrl+"/"+siteId+"/pq/api/job-statuses/"
       // var BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/pq/api/job-statuses/"

        val apiService = RetrofitClient(context).getRetrofitInstance(BASE_URL).create(ApiService::class.java)

        val call = if(IsLdap.equals("LDAP")){
         apiService.getPrintJobStatusesForLdap(
             siteId.toString(),
             LdapUsername.toString(),
             LdapPassword.toString(),
             "printerDeviceQueue.printers"
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
                                DataDogLogger.getLogger().i("Devnco_Android printer" + "it=>${it}")
                                listUpdate(it as ArrayList<SelectedFile>?, requireContext())
                            },
                            {
                                Log.i("printer", "Error=>${it.message}")
                                DataDogLogger.getLogger()
                                    .i("Devnco_Android printer" + "Error=>${it.message}")
                            }
                        )
                    compositeDisposable.add(disposable4)
                    isFileSelected = true
                    Log.i("printer", "list of Files-->$list")
                    DataDogLogger.getLogger().i("Devnco_Android printer" + "list of Files-->$list")

                }

            }

            override fun onFailure(call: Call<GetJobStatusesResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_SHORT).show()
                Log.i("printer", t.message.toString())
                DataDogLogger.getLogger().i("Devnco_Android printer" + t.message.toString())
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
                        DataDogLogger.getLogger().i("Devnco_Android printer" + "it=>${it}")
                        isFileSelected = true
                        bundle.putSerializable("selectedFileList", it as ArrayList<SelectedFile>)
                        listUpdate(it as ArrayList<SelectedFile>?, requireContext())
                    },
                    {
                        Log.i("printer", "Error=>${it.message}")
                        DataDogLogger.getLogger()
                            .i("Devnco_Android printer" + "Error=>${it.message}")
                    }
                )
            compositeDisposable.add(disposable3)
            isFileSelected = true
            Log.i("printer", "list of Files-->$list")
            DataDogLogger.getLogger().i("Devnco_Android printer" + "list of Files-->$list")
        }
    }


    override fun onDestroy() {
        super.onDestroy()
      //  compositeDisposable.clear()
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
            DataDogLogger.getLogger().e("Devnco_Android exception" + ex.toString())
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
                DataDogLogger.getLogger()
                    .i("Devnco_Android printer" + "response validate token=>${response.isSuccessful}")
                Log.i("printer", "response validate token=>${response}")
                DataDogLogger.getLogger()
                    .i("Devnco_Android printer" + "response validate token=>${response}")
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Log.i("printer", "response validate token Error=>${t.message}")
                DataDogLogger.getLogger()
                    .i("Devnco_Android printer" + "response validate token Error=>${t.message}")
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
            DataDogLogger.getLogger().i("Devnco_Android printer" + "item checked ===>${it}")
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
        val tanentUrl = getTenantUrl(context)
        var  BASE_URL = ""+tanentUrl+"/"+siteId+"/pq/api/job-statuses/"
        //var  BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/pq/api/job-statuses/"

        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call = if(IsLdap.equals("LDAP")){
            apiService.getPrintJobStatusesForLdap(
                siteId.toString(),
                LdapUsername.toString(),
                LdapPassword.toString(),
                "printerDeviceQueue.printers"
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
                                DataDogLogger.getLogger().i("Devnco_Android printer" + "it=>${it}")
                                //  listUpdate(it as ArrayList<SelectedFile>?, context)
                            },
                            {
                                Log.i("printer", "Error=>${it.message}")
                                DataDogLogger.getLogger()
                                    .i("Devnco_Android printer" + "Error=>${it.message}")
                            }
                        )
                    compositeDisposable.add(disposable4)
                    isFileSelected = true
                    Log.i("printer", "list of Files-->$list")
                    DataDogLogger.getLogger().i("Devnco_Android printer" + "list of Files-->$list")

                }

            }

            override fun onFailure(call: Call<GetJobStatusesResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_SHORT).show()
                Log.i("printer", t.message.toString())
                DataDogLogger.getLogger().i("Devnco_Android printer" + t.message.toString())
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
fun sendMetaData(context: Context, TotalPageCount: Int, colorMode: Int){
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
        DataDogLogger.getLogger().i("Devnco_Android ipAddress of device:" + ipAddress);
    }
    var BASE_URL =""
    val companyUrl = LoginPrefs.getCompanyUrl(context)
    val siteId= LoginPrefs.getSiteId(context)
    val xIdpType =SignInCompanyPrefs.getIdpType(context)
    val xIdpName =SignInCompanyPrefs.getIdpName(context)
    val dateTime:String =DateTimeConversion.currentDateTime();

    val idpInfo ="{\"os\":"+"android"+",\"idpName\":"+xIdpName+",\"username\":"+username+",\"isLoggedIn\":"+true+",\"type\":"+xIdpType+",\"token\":"+LoginPrefs.getOCTAToken(
        context
    )+"}";
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
            "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                    " <printjobs>\n" +
                    " <machine>\n" +
                    " <commonnames>\n" +
                    "<name>" + ipAddress + "</name>\n" +
                    "</commonnames>\n" +
                    "</machine>\n" +
                    "<jobs>\n" +
                    "<job iscolor=\"" + colorMode + "\" istcpip=\"1\">\n" +
                    " <printer id=\"" + printerId + "\">\n" +
                    "<name>" + printerName + "</name>\n" +
                    "<share />\n" +
                    "</printer>\n" +
                    "<source>\n" +
                    "<user>" + username + "</user>\n" +
                    "<machine>" + ipAddress + "</machine>\n" +
                    "<ip_address>" + ipAddress + "</ip_address>\n" +
                    "<mgr />\n" +
                    "<dpt />\n" +
                    "<com />\n" +
                    "<fn />\n" +
                    " <jt />\n" +
                    "<aun>" + URLEncoder.encode(idpInfo) + "</aun>\n" +
                    "</source>\n" +
                    "<document pl=\"2794\" pw=\"2159\" duplex=\"2\" length=\"" + TotalPageCount + "\">\n" +
                    "<submitted>" + dateTime + "</submitted>\n" +
                    "<completed>" + dateTime + "</completed>\n" +
                    "<title>" + fileName + "</title>\n" +
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
            "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                    " <printjobs>\n" +
                    " <machine>\n" +
                    " <commonnames>\n" +
                    "<name>" + ipAddress + "</name>\n" +
                    "</commonnames>\n" +
                    "</machine>\n" +
                    "<jobs>\n" +
                    "<job iscolor=\"" + colorMode + "\" istcpip=\"1\">\n" +
                    " <printer id=\"" + printerId + "\">\n" +
                    "<name>" + printerName + "</name>\n" +
                    "<share />\n" +
                    "</printer>\n" +
                    "<source>\n" +
                    "<user>" + username + "</user>\n" +
                    "<machine>" + ipAddress + "</machine>\n" +
                    "<ip_address>" + ipAddress + "</ip_address>\n" +
                    "<mgr />\n" +
                    "<dpt />\n" +
                    "<com />\n" +
                    "<fn />\n" +
                    " <jt />\n" +
                    "<aun>" + URLEncoder.encode(idpInfo) + "</aun>\n" +
                    "</source>\n" +
                    "<document pl=\"2794\" pw=\"2159\" duplex=\"2\" length=\"" + TotalPageCount + "\">\n" +
                    "<submitted>" + dateTime + "</submitted>\n" +
                    "<completed>" + dateTime + "</completed>\n" +
                    "<title>" + fileName + "</title>\n" +
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
                    DataDogLogger.getLogger()
                        .e("Devnco_Android printer" + "e=>${e.message.toString()}")
                }
            } else {
                ProgressDialog.cancelLoading()

            }
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            ProgressDialog.cancelLoading()

            Log.i("printer", "Error html response==>${t.message.toString()}")
            DataDogLogger.getLogger()
                .i("Devnco_Android printer" + "Error html response==>${t.message.toString()}")
        }
    })
}




    fun sendHeldJob(context: Context, fileName: String,fileSize:String,totalPageCount:String,printerId:String,isPullPrinter:String,purpose:String){
        @SuppressLint("WrongConstant")val sh: SharedPreferences = context.getSharedPreferences(
            "MySharedPref",
            Context.MODE_APPEND
        )
        val ipAddress =IpAddress.getLocalIpAddress();

        if(ipAddress!=null) {
            Log.d("ipAddress of device:", ipAddress);
            DataDogLogger.getLogger().i("Devnco_Android ipAddress of device for send held job:" + ipAddress);
        }
        var printType="1"
        if(isPullPrinter.equals("0")){
            printType="1"
        }else{
            printType="2"
        }
       // showLoadingDialog(context, "please wait")
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")
        var BASE_URL =""
        val companyUrl = LoginPrefs.getCompanyUrl(context)
        val siteId= LoginPrefs.getSiteId(context)
        val xIdpType =SignInCompanyPrefs.getIdpType(context)
        val xIdpName =SignInCompanyPrefs.getIdpName(context)
        val uuid = UUID.randomUUID()
        val randomUUIDString = uuid.toString()
        val guid=randomUUIDString
        val oldGuid = LoginPrefs.getGuId(context)
        val jobId="1"
        var usernName=""
        if(IsLdap.equals("LDAP")){
                usernName=LdapUsername.toString()
        }else {
            usernName = decodeJWT(context)
        }
        val dateTime:String =DateTimeConversion.currentDateTime();
        val documentTitle=fileName

        val workStationId: String? =LoginPrefs.getworkSatationId(context)
        Log.d("workStationId pref: ", workStationId.toString())
        var data =""
        if(workStationId == null || workStationId =="") {
            LoginPrefs.saveJobId(context, jobId)
            LoginPrefs.saveGuId(context,guid)
            var macAddress:String=ServerPrintRelaseFragment.getMacAddress(context)
            Log.d("macAddress:", macAddress)
            PrintPreview.setJobId(context,jobId.toString(),fileName);
            Log.d("held data:", "workstationId not Present")
            addHeldJobs(context,jobId,dateTime,documentTitle,fileSize,totalPageCount,printType,printerId,usernName)

            data = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                    "<data mac=\"10.0.0.4\" w=\"\" >\n" +
                    "<register dns=\"\" ip4=\""+ipAddress+"\" nb=\""+macAddress+"\"/>\n" +
                    "<full guid=\"" + guid + "\">\n" +
                    "<queue pid=\"" + printerId + "\" ptype=\"0\" >\n" +
                    "<job a=\"a\" wjid=\"" + jobId + "\" un=\"" + usernName + "\" mn=\"Mobile\" sub=\"" + dateTime + "\" dt=\"" + documentTitle + "\" sd=\"Held\" sz= \"" + fileSize + "\" p=\"" + totalPageCount + "\" t=\"" + printType + "\"/>\n" +
                    "</queue>\n" +
                    "</full>\n" +
                    "</data>"
        }else if(purpose.equals("fullPackageRequired")){
            Log.d("delta data:", "delta data send")
            var jobID : String? = LoginPrefs.getLastJobId(context);
            LoginPrefs.saveGuId(context,guid)
            var jobIdentity=0
            if(jobID==null){
                jobIdentity=jobId.toInt();
            }else{
                val jobIds= jobID?.toInt();
                jobIdentity = jobIds?.plus(1)
                LoginPrefs.saveJobId(context, jobIdentity.toString())
            }
            PrintPreview.setJobId(context,jobIdentity.toString(),fileName);
            val documentTitle=fileName

            data = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                    "<data mac=\"10.0.0.4\" w=\""+workStationId+"\" >\n" +
                    "<full guid=\"" + guid + "\">\n" +
                    "<queue pid=\"" + printerId + "\" ptype=\"0\" >\n" +
                    "<job a=\"a\" wjid=\"" + jobIdentity + "\" un=\"" + usernName + "\" mn=\"Mobile\" sub=\"" + dateTime + "\" dt=\"" + documentTitle + "\" sd=\"Held\" sz= \"" + fileSize + "\" p=\"" + totalPageCount + "\" t=\"" + printType + "\"/>\n" +
                    "</queue>\n" +
                    "</full>\n" +
                    "</data>"

        }else{
            Log.d("delta data:", "delta data send")
            var jobID : String? = LoginPrefs.getLastJobId(context);
            LoginPrefs.saveGuId(context,guid)
            var jobIdentity=0
            if(jobID==null){
                jobIdentity=jobId.toInt();
            }else{
            val jobIds= jobID?.toInt();
             jobIdentity = jobIds?.plus(1)
            LoginPrefs.saveJobId(context, jobIdentity.toString())
            }
            PrintPreview.setJobId(context,jobIdentity.toString(),fileName);
            val documentTitle=fileName

            addHeldJobs(context,jobIdentity.toString(),dateTime,documentTitle,fileSize,totalPageCount,printType,printerId,usernName)
            data = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                    "<data mac=\"10.0.0.4\" w=\""+workStationId+"\" >\n" +
                    "<delta basedon=\""+oldGuid+"\" guid=\""+guid+"\">\n" +
                    "<queue pid=\"" + printerId + "\" ptype=\"0\" >\n" +
                    "<job a=\"a\" wjid=\"" + jobIdentity + "\" un=\"" + usernName + "\" mn=\"Mobile\" sub=\"" + dateTime + "\" dt=\"" + documentTitle + "\" sd=\"Held\" sz= \"" + fileSize + "\" p=\"" + totalPageCount + "\" t=\"" + printType + "\"/>\n" +
                    "</queue>\n" +
                    "</delta>\n" +
                    "</data>"
        }



        BASE_URL = "https://"+companyUrl+"/state/query/client_requests.php/"
        val apiService = RetrofitClient(context).getRetrofitInstance(BASE_URL).create(ApiService::class.java)

        val call = if(IsLdap.equals("LDAP")){
            apiService.sendHeldJobForLdap(
                siteId.toString(),
                LdapUsername.toString(),
                LdapPassword.toString(),
                data
            )
        }else if(siteId.toString().contains("google")){
            DataDogLogger.getLogger().i(
                "Devnco_Android API call: " + BASE_URL.toString() + " Token: " + LoginPrefs.getOCTAToken(
                    context
                ) + " username: " + usernName
            )

            apiService.sendHeldJobForGoogle(
                siteId.toString(),
                "Bearer ${LoginPrefs.getOCTAToken(context)}",
                usernName,
                xIdpType.toString(),
                xIdpName.toString(),
                "serverId",
                data

            )
        }else{
            DataDogLogger.getLogger().i(
                "Devnco_Android API call: " + BASE_URL.toString() + " Token: " + LoginPrefs.getOCTAToken(
                    context
                ) + " username: " + usernName
            )

            apiService.sendHeldJobForOtherIdp(
                siteId.toString(),
                "Bearer ${LoginPrefs.getOCTAToken(context)}",
                usernName,
                xIdpType.toString(),
                xIdpName.toString(),
                data


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
                        Log.d("response of held job:",html.toString())

                        val document = Jsoup.parse(html, "", Parser.xmlParser())
                        Log.d("parse held job:",document.toString())
                        val error = document.select("e")
                        val errormassage:String =error.toString()
                        Log.d("errormsg in held job:",errormassage.toString())
                        if(errormassage.contains("There was an error applying the delta information. Full package required")){
                          //  LoginPrefs.saveworkSatationId(context, "");
                            sendHeldJob(context,fileName,fileSize,totalPageCount,printerId,isPullPrinter,"fullPackageRequired")
                        }

                        if(errormassage.contains("The workstation needs to be registered")){
                            LoginPrefs.saveworkSatationId(context, "");
                            sendHeldJob(context,fileName,fileSize,totalPageCount,printerId,isPullPrinter,"")
                        }


                        val element = document.select("workstation")

                        Log.d("workstationId:", element.text())
                        val workStationId = element.text()
                        if(workStationId != null && workStationId !="") {
                            LoginPrefs.saveworkSatationId(context, workStationId)
                        }
                        cancelLoading()

                    } catch (e: Exception) {
                        Log.i("printer", "e=>${e.message.toString()}")
                        DataDogLogger.getLogger()
                            .e("Devnco_Android held print job" + "e=>${e.message.toString()}")
                        cancelLoading()
                    }
                } else {
                    cancelLoading()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Log.i("printer", "Error html response==>${t.message.toString()}")
                DataDogLogger.getLogger()
                    .i("Devnco_Android hold print job: " + "Error html response==>${t.message.toString()}")
            }
        })
    }

//****************************************************** every 7 sec call ********************************************

    fun gettingHeldJobStatus(context: Context,purpose:String){
        @SuppressLint("WrongConstant")val sh: SharedPreferences = context.getSharedPreferences(
            "MySharedPref",
            Context.MODE_APPEND
        )
        val ipAddress =IpAddress.getLocalIpAddress();

        if(ipAddress!=null) {
            Log.d("ipAddress of device:", ipAddress);
            DataDogLogger.getLogger().i("Devnco_Android ipAddress of device for send held job:" + ipAddress);
        }

        // showLoadingDialog(context, "please wait")
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")
        var BASE_URL =""
        val companyUrl = LoginPrefs.getCompanyUrl(context)
        val siteId= LoginPrefs.getSiteId(context)
        val xIdpType =SignInCompanyPrefs.getIdpType(context)
        val xIdpName =SignInCompanyPrefs.getIdpName(context)
        val uuid = UUID.randomUUID()
        val randomUUIDString = uuid.toString()
        val guid=randomUUIDString
        val oldGuid = LoginPrefs.getGuId(context)
        var usernName=""
        if(IsLdap.equals("LDAP")){
            usernName=LdapUsername.toString()
        }else {
            usernName = decodeJWT(context)
        }

        val workStationId: String? =LoginPrefs.getworkSatationId(context)
        Log.d("workStationId pref: ", workStationId.toString())
        var data =""
      /*  if(workStationId == null || workStationId =="") {

            LoginPrefs.saveGuId(context,guid)
            Log.d("held data:", "workstationId not Present")
           data = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                    "<data mac=\"10.0.0.4\" w=\"\" >\n" +
                    "<register dns=\"\" ip4=\""+ipAddress+"\" nb=\"Mobile99\"/>\n" +
                    "<full guid=\"" + guid + "\">\n" +
                    "</full>\n" +
                    "</data>"
        }
        else {*/
            LoginPrefs.saveGuId(context,guid)
        var oldHoldJobs:String=sendOldHeldJob(context)
        Log.d("oldHoldJobs",oldHoldJobs)

        if(purpose.equals("FullPackageNeed")){
            data = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                    "<data mac=\"10.0.0.4\" w=\""+workStationId+"\" >\n" +
                    "<full guid=\"" + guid + "\">\n" +
                    oldHoldJobs+
                    "</full>\n" +
                    "</data>"

        }else {
            data = "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                    "<data mac=\"10.0.0.4\" w=\"" + workStationId + "\" >\n" +
                    "<delta basedon=\"" + oldGuid + "\" guid=\"" + guid + "\">\n" +
                    "</delta>\n" +
                    "</data>"
        }
    //    }

        BASE_URL = "https://"+companyUrl+"/state/query/client_requests.php/"
        val apiService = RetrofitClient(context).getRetrofitInstance(BASE_URL).create(ApiService::class.java)

        val call = if(IsLdap.equals("LDAP")){
            apiService.sendHeldJobForLdap(
                siteId.toString(),
                LdapUsername.toString(),
                LdapPassword.toString(),
                data
            )
        }else if(siteId.toString().contains("google")){
            DataDogLogger.getLogger().i(
                "Devnco_Android API call: " + BASE_URL.toString() + " Token: " + LoginPrefs.getOCTAToken(
                    context
                ) + " username: " + usernName
            )

            apiService.sendHeldJobForGoogle(
                siteId.toString(),
                "Bearer ${LoginPrefs.getOCTAToken(context)}",
                usernName,
                xIdpType.toString(),
                xIdpName.toString(),
                "serverId",
                data

            )
        }else{
            DataDogLogger.getLogger().i(
                "Devnco_Android API call: " + BASE_URL.toString() + " Token: " + LoginPrefs.getOCTAToken(
                    context
                ) + " username: " + usernName
            )

            apiService.sendHeldJobForOtherIdp(
                siteId.toString(),
                "Bearer ${LoginPrefs.getOCTAToken(context)}",
                usernName,
                xIdpType.toString(),
                xIdpName.toString(),
                data


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
                        Log.d("response of held job:",html.toString())

                        val document = Jsoup.parse(html, "", Parser.xmlParser())
                        Log.d("parse held job:",document.toString())
                        val error = document.select("e")
                        val errormassage:String =error.toString()
                        Log.d("errormsg in held job:",errormassage.toString())
                       if(errormassage.contains("There was an error applying the delta information. Full package required")){
                           // LoginPrefs.saveworkSatationId(context, "");
                            gettingHeldJobStatus(context,"FullPackageNeed")
                        }


                     val release =document.select("release")
                        if(!release.isEmpty()){
                            val data = document.select("m")

                            Log.i("printer", "release data==>"+data.toString())

                            data.forEach {
                                var jobId=""
                                var printerId=""
                                var printerName=""
                                var printerHost=""
                                var jobstatus =""

                                jobId =it.attr("data")
                                jobstatus=it.attr("m")

                              release.forEach {

                                printerId=it.attr("pid")
                                printerName=it.attr("title")

                            }

                            val address =document.select("address")
                            if(address != null) {
                                address.forEach {
                                    printerHost=it.attr("host")
                                }
                            }
                                if(data.size > 1) {
                                    removeHeldJobs(context,jobId)
                                    if(jobstatus.equals("2")) {
                                        Handler().postDelayed(
                                            {
                                                Log.i(
                                                    "printer",
                                                    "it multi job Name==>" + printerName
                                                )
                                                Log.i("printer", "it multi job jobId==>" + jobId)
                                                Log.i(
                                                    "printer",
                                                    "it multi job printerId==>" + printerId
                                                )
                                                Log.i(
                                                    "printer",
                                                    "it multi job printerHost==>" + printerHost
                                                )

                                                ServerPrintRelaseFragment.getjobFromSharedPreferencesForPullJobs(
                                                    context,
                                                    jobId,
                                                    printerId,
                                                    printerName,
                                                    printerHost
                                                )
                                            },
                                            10000
                                        )
                                    }

                                }else{
                                    removeHeldJobs(context,jobId)
                                    if(jobstatus.equals("2")) {
                                        Log.i("printer", "it single job Name==>" + printerName)
                                        Log.i("printer", "it single job jobId==>" + jobId)
                                        Log.i("printer", "it single job printerId==>" + printerId)
                                        Log.i(
                                            "printer",
                                            "it single job printerHost==>" + printerHost
                                        )
                                        ServerPrintRelaseFragment.getjobFromSharedPreferencesForPullJobs(
                                            context,
                                            jobId,
                                            printerId,
                                            printerName,
                                            printerHost
                                        )
                                    }
                                }
                            }

                        }
                        else {

                            val data = document.select("m")

                            data.forEach {
                                Log.i("printer", "it data==>${it.attr("data")}")
                                Log.i("printer", "it ptype==>${it.attr("ptype")}")
                                Log.i("printer", "it pid==>${it.attr("pid")}")
                                Log.i("printer", "it m==>${it.attr("m")}")

                            }
                            if (data.size > 0) {
                                data.forEach {

                                    if (data.size > 1) {
                                        removeHeldJobs(context,it.attr("data"))
                                        if(it.attr("m").equals("2")) {
                                            Handler().postDelayed(
                                                {
                                                    ServerPrintRelaseFragment.getjobFromSharedPreferences(
                                                        context, it.attr("data"), it.attr("pid")
                                                    )
                                                },
                                                10000
                                            )
                                        }
                                    } else {
                                        removeHeldJobs(context,it.attr("data"))
                                        if(it.attr("m").equals("2")) {
                                            ServerPrintRelaseFragment.getjobFromSharedPreferences(
                                                context, it.attr("data"), it.attr("pid")
                                            )
                                        }
                                    }

                                }
                            }
                        }

                        val element = document.select("workstation")

                        Log.d("workstationId:", element.text())
                        val workStationId = element.text()
                       if(workStationId != null && workStationId !="") {
                            LoginPrefs.saveworkSatationId(context, workStationId)
                        }
                        cancelLoading()

                    } catch (e: Exception) {
                        Log.i("printer", "e=>${e.message.toString()}")
                        DataDogLogger.getLogger()
                            .e("Devnco_Android held print job" + "e=>${e.message.toString()}")
                        cancelLoading()
                    }
                } else {
                    cancelLoading()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Log.i("printer", "Error html response==>${t.message.toString()}")
                DataDogLogger.getLogger()
                    .i("Devnco_Android hold print job: " + "Error html response==>${t.message.toString()}")
            }
        })
    }


    fun addHeldJobs(context:Context,jobId:String,dateTime:String,documentTitle:String,size:String,TotalPageCount:String,printType:String,printerId:String,username:String){
        val ServerJobsModelList =java.util.ArrayList<ServerJobsModel>()
        var serverJobsModel =ServerJobsModel()
        serverJobsModel.jobId=jobId
        serverJobsModel.dateTime=dateTime
        serverJobsModel.documentTitle =documentTitle
        serverJobsModel.size=size
        serverJobsModel.totalPageCount=TotalPageCount
        serverJobsModel.printType=printType
        serverJobsModel.printerId=printerId
        serverJobsModel.userName=username
        ServerJobsModelList.add(serverJobsModel)

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val gson = Gson()
        val json = prefs.getString("prefaddedServerJobsModelList", null)
        val type = object :
            TypeToken<java.util.ArrayList<ServerJobsModel?>?>() {}.type
        var sharedPreferencesServerJobsModelList = java.util.ArrayList<ServerJobsModel>()
        if (json != null) {
            sharedPreferencesServerJobsModelList = gson.fromJson<java.util.ArrayList<ServerJobsModel>>(
                json,
                type
            )
        }
        sharedPreferencesServerJobsModelList.addAll(ServerJobsModelList)

        val editor = prefs.edit()
        val json1 = gson.toJson(sharedPreferencesServerJobsModelList)
        editor.putString("prefaddedServerJobsModelList", json1)
        editor.apply()

    }

    fun sendOldHeldJob(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val gson = Gson()
        val json = prefs.getString("prefaddedServerJobsModelList", null)
        val type = object :
            TypeToken<java.util.ArrayList<ServerJobsModel?>?>() {}.type
        var sharedPreferencesServerJobsModelList = java.util.ArrayList<ServerJobsModel>()
        if (json != null) {
            sharedPreferencesServerJobsModelList = gson.fromJson<java.util.ArrayList<ServerJobsModel>>(
                json,
                type
            )
        }

        var oldHeldJob:String=""
        for (item in sharedPreferencesServerJobsModelList) {
         Log.d("jobId:",item.jobId)
         Log.d("dateTime:",item.dateTime)
         Log.d("documentTitle:",item.documentTitle)
         Log.d("size:",item.size)
         Log.d("totalPageCount:",item.totalPageCount)
         Log.d("printType:",item.printType)
            var printerId =item.printerId
            var jobIdentity =item.jobId
            var usernName =item.userName
            var dateTime =item.dateTime
            var documentTitle =item.documentTitle
            var fileSize=item.size
            var totalPageCount =item.totalPageCount
            var printType =item.printType

            oldHeldJob=oldHeldJob +"<queue pid=\"" + printerId + "\" ptype=\"0\" >\n" +
                    "<job a=\"a\" wjid=\"" + jobIdentity + "\" un=\"" + usernName + "\" mn=\"Mobile\" sub=\"" + dateTime + "\" dt=\"" + documentTitle + "\" sd=\"Held\" sz= \"" + fileSize + "\" p=\"" + totalPageCount + "\" t=\"" + printType + "\"/>\n" +
                    "</queue>\n"

        }
        Log.d("oldHeldJob:",oldHeldJob)
        return oldHeldJob
    }


    fun removeHeldJobs(context:Context,jobId:String){
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val gson = Gson()
        val json = prefs.getString("prefaddedServerJobsModelList", null)
        val type = object :
            TypeToken<java.util.ArrayList<ServerJobsModel?>?>() {}.type
        var sharedPreferencesServerJobsModelList = java.util.ArrayList<ServerJobsModel>()
        if (json != null) {
            sharedPreferencesServerJobsModelList = gson.fromJson<java.util.ArrayList<ServerJobsModel>>(
                json,
                type
            )
        }

               var removeServerJobsModel:ServerJobsModel=ServerJobsModel()
            for ((i, item) in sharedPreferencesServerJobsModelList.withIndex()) {
                var serverJobsModel:ServerJobsModel= sharedPreferencesServerJobsModelList.get(i)
                if(serverJobsModel.jobId.equals(jobId)){
                    removeServerJobsModel =serverJobsModel
                }
            }
        if(removeServerJobsModel !=null) {
            sharedPreferencesServerJobsModelList.remove(removeServerJobsModel)
        }
        val editor = prefs.edit()
        val json1 = gson.toJson(sharedPreferencesServerJobsModelList)
        editor.putString("prefaddedServerJobsModelList", json1)
        editor.apply()

    }
}

//https://www.youtube.com/watch?v=vPLKNsQEAEc
//https://www.youtube.com/watch?v=nC9E9dvw2eY
