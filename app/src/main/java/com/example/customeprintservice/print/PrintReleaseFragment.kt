package com.example.customeprintservice.print

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
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
import com.example.customeprintservice.jipp.QRCodeScanActivity
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
import com.example.customeprintservice.utils.JwtDecode
import com.example.customeprintservice.utils.PermissionHelper
import com.example.customeprintservice.utils.Permissions
import com.example.customeprintservice.utils.ProgressDialog
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_print_release.*
import org.jetbrains.anko.doAsync
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class PrintReleaseFragment : Fragment() {

    private var permissionsHelper: PermissionHelper? = null
    private val bundle = Bundle()
    private var isFileSelected: Boolean = false
    private var list = ArrayList<SelectedFile>()
//    private val rxPermissions = RxPermissions(this)

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
    companion object {
        public val getdocumentList = java.util.ArrayList<SelectedFile>()
    }

    var selectedServerFilelist = ArrayList<SelectedFile>()
     var localdocumentFromsharedPrefences = ArrayList<SelectedFile>()
    @SuppressLint("CheckResult")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
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
            }, {
                it.message
            })
        compositeDisposable.add(disposable)

        return inflater.inflate(R.layout.fragment_print_release, container, false)
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
       // (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        /**
        check read/write permission
         */
        checkPermissions()
        /**
         * validate token
         */
//        validateToken()




        ProgressDialog.showLoadingDialog(requireContext(), "Getting Hold jobs")
        getJobStatuses(
            requireContext(),
            decodeJWT(),
            SignInCompanyPrefs.getIdpType(requireContext()).toString(),
            SignInCompanyPrefs.getIdpName(requireContext()).toString()
        )
/*
        btnRelease.setOnClickListener {
            ProgressDialog.showLoadingDialog(requireContext(), "Released Job")
            releaseJob(requireContext())
        }
*/
        /**
         * Print Job status cancel
         */

        drawer.setOnClickListener {
               val intent = Intent(context, MainActivity::class.java)
             startActivity(intent)
        }
        serverPrintScreen.setOnClickListener {
            val intent = Intent(context, BottomNavigationActivityForServerPrint::class.java)
            startActivity(intent)
        }
        val printersFragment =PrintersFragment()
        printersFragment.getPrinterList(requireContext(),decodeJWT());
       /* btnDeleteJobs.setOnClickListener {
            ProgressDialog.showLoadingDialog(requireContext(), "Cancel Job")
           // cancelJob()
        }*/
      /*  imgLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Click on Logout", Toast.LENGTH_SHORT).show()
            LoginPrefs.deleteToken(requireContext())
            val sharedPreferences: SharedPreferences =
                requireContext().getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
            val myEdit = sharedPreferences.edit()
            myEdit.putString("IsLdap","Others");
            myEdit.commit()

            val intent = Intent(requireContext(), SignInCompany::class.java)
            startActivity(intent)
            activity?.finish()
        }

       */

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
            deleteJobsItem.jobType = "1"
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
        }else{
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
            }
        })
    }

    fun releaseJob(context: Context){
        @SuppressLint("WrongConstant")val sh: SharedPreferences =
            context.getSharedPreferences("MySharedPref", Context.MODE_APPEND)
        val IsLdap = sh.getString("IsLdap", "")
        val LdapUsername= sh.getString("LdapUsername", "")
        val LdapPassword= sh.getString("LdapPassword", "")

        Log.d("IsLdap:", IsLdap!!)

        ProgressDialog.showLoadingDialog(context, "Released Job")
        releaseJobCheckedListForServer = BottomNavigationActivityForServerPrint.selectedServerFile as ArrayList<SelectedFile>
        val siteId= LoginPrefs.getSiteId(context)
        var BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/pq/api/job-statuses/release/"



        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val releaseJobRequest = ReleaseJobRequest()
        val releaseJobs = ArrayList<ReleaseJobsItem>()
        releaseJobCheckedListForServer.forEach {
            val releaseJobsItem = ReleaseJobsItem()
            releaseJobsItem.jobNum = it.jobNum
            releaseJobsItem.jobType = "1"
            releaseJobsItem.queueId = it.queueId
            releaseJobsItem.userName = it.userName
            releaseJobsItem.workstationId = it.workStationId
            releaseJobs.add(releaseJobsItem)
        }
        releaseJobRequest.releaseJobs = releaseJobs

        val call = if(IsLdap.equals("LDAP")){
            apiService.releaseJobForLdap(
                releaseJobRequest,
                siteId.toString(),
                LdapUsername.toString(),
                LdapPassword.toString()
            )
        }else{
            apiService.releaseJob(
                releaseJobRequest, "Bearer " + LoginPrefs.getOCTAToken(context),
                decodeJWT(context),
                SignInCompanyPrefs.getIdpType(context).toString(),
                SignInCompanyPrefs.getIdpName(context).toString()
            )
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
                    BottomNavigationActivityForServerPrint.selectedServerFile.clear()
                    val activity: Activity? = activity
                    if (activity != null) {

                    }

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
            }

        })
    }



    fun getJobStatuses(context: Context, userName: String, idpType: String, idpName: String) {
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
             siteId.toString(),
             LdapUsername.toString(),
             LdapPassword.toString()
         )
        }else{
            apiService.getPrintJobStatuses(
               "Bearer " + LoginPrefs.getOCTAToken(context),
               userName,
               idpType,
               idpName,userName)
       }

        call.enqueue(object : Callback<GetJobStatusesResponse> {
            override fun onResponse(
                call: Call<GetJobStatusesResponse>,
                response: Response<GetJobStatusesResponse>
            ) {

                val getJobStatusesResponse = response.body()?.printQueueJobStatus
                if (getJobStatusesResponse?.size == 0) {


                    getdocumentList.clear()
                    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    val gson = Gson()
                    val jsonText: String? = prefs.getString("localdocumentlist", null)
                    val type: Type = object : TypeToken<java.util.ArrayList<SelectedFile?>?>() {}.getType()
                   if(jsonText !=null) {
                       localdocumentFromsharedPrefences = gson.fromJson(jsonText, type)

                       // ServerPrintRelaseFragment.serverDocumentlist.addAll(localdocumentFromsharedPrefences);
                       getdocumentList.addAll(localdocumentFromsharedPrefences)
                   }
                    ProgressDialog.cancelLoading()
                } else {
                    ProgressDialog.cancelLoading()
                    val parseList: List<PrintQueueJobStatusItem?>? =
                        getJobStatusesResponse
                    val disposable4 = Observable.fromCallable {
                        val selectedFileList = ArrayList<SelectedFile>()
                        //ServerPrintRelaseFragment.serverDocumentlist.clear()
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
                            selectedFileList.add(selectedFile)
                           // ServerPrintRelaseFragment.serverDocumentlist.add(selectedFile)
                            getdocumentList.add(selectedFile)
                        }
                        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                        val gson = Gson()
                        val jsonText: String? = prefs.getString("localdocumentlist", null)
                        val type: Type = object : TypeToken<java.util.ArrayList<SelectedFile?>?>() {}.getType()
                        localdocumentFromsharedPrefences =gson.fromJson(jsonText, type)

                        //ServerPrintRelaseFragment.serverDocumentlist.addAll(localdocumentFromsharedPrefences);
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
                                listUpdate(it as ArrayList<SelectedFile>?, requireContext())
                            },
                            {
                                Log.i("printer", "Error=>${it.message}")
                            }
                        )
                    compositeDisposable.add(disposable4)
                    isFileSelected = true
                    Log.i("printer", "list of Files-->$list")

                }

            }

            override fun onFailure(call: Call<GetJobStatusesResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_SHORT).show()
                Log.i("printer", t.message.toString())
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
                        isFileSelected = true
                        bundle.putSerializable("selectedFileList", it as ArrayList<SelectedFile>)
                        listUpdate(it as ArrayList<SelectedFile>?, requireContext())
                    },
                    {
                        Log.i("printer", "Error=>${it.message}")
                    }
                )
            compositeDisposable.add(disposable3)
            isFileSelected = true
            Log.i("printer", "list of Files-->$list")
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
        } catch (ex: Exception) {
           // context.toast("Failed to Decode Jwt Token")
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
                Log.i("printer", "response validate token=>${response}")
            }

            override fun onFailure(call: Call<Any>, t: Throwable) {
                Log.i("printer", "response validate token Error=>${t.message}")
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
            releaseJobCheckedList.add(it)
        }?.subscribe()
        recyclerViewDocumentList?.adapter = adapter


//
//        adapter?.setListener(object :
//            FragmentSelectedFileListAdapter.ViewHolder.FragmentSelectedFileAdapterListener {
//            override fun onItemClick(position: Int) {
////                enableActionMode(position, context)
//            }
//
//            override fun onItemLongClick(position: Int) {
////                enableActionMode(position, context)
//            }
//        })
//    }
//
//    private var actionMode: ActionMode? = null
//
//    private fun enableActionMode(position: Int, context: Context) {
//
//        if (actionMode == null)
//            actionMode = AppCompatActivity().startSupportActionMode(object : ActionMode.Callback {
//                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
//                    mode.menuInflater.inflate(R.menu.menu_delete, menu)
//                    return true
//                }
//
//                override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
//                    return false
//                }
//
//                override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
//                    if (item.itemId == R.id.action_delete) {
//
//                        mode.finish()
//                        return true
//                    }
//                    return false
//                }
//
//                override fun onDestroyActionMode(mode: ActionMode) {
//
//                    adapter?.notifyDataSetChanged()
//                    actionMode = null
//                }
//            })

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
        }else{
            apiService.getPrintJobStatuses(
                "Bearer " + LoginPrefs.getOCTAToken(context),
                decodeJWT(context),
                SignInCompanyPrefs.getIdpType(context).toString(),
                SignInCompanyPrefs.getIdpName(context).toString(),
                decodeJWT(context))
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

                    val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                    val gson = Gson()
                    val jsonText: String? = prefs.getString("localdocumentlist", null)
                    val type: Type = object : TypeToken<java.util.ArrayList<SelectedFile?>?>() {}.getType()
                    localdocumentFromsharedPrefences =gson.fromJson(jsonText, type)

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
                        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
                        val gson = Gson()
                        val jsonText: String? = prefs.getString("localdocumentlist", null)
                        val type: Type = object : TypeToken<java.util.ArrayList<SelectedFile?>?>() {}.getType()
                        localdocumentFromsharedPrefences =gson.fromJson(jsonText, type)

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
                                //  listUpdate(it as ArrayList<SelectedFile>?, context)
                            },
                            {
                                Log.i("printer", "Error=>${it.message}")
                            }
                        )
                    compositeDisposable.add(disposable4)
                    isFileSelected = true
                    Log.i("printer", "list of Files-->$list")

                }

            }

            override fun onFailure(call: Call<GetJobStatusesResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Toast.makeText(requireContext(), t.message.toString(), Toast.LENGTH_SHORT).show()
                Log.i("printer", t.message.toString())
            }
        })



    }

    @SuppressLint("WrongConstant")
    fun dialogSuccessfullyPrint(context:Context) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_successful_print)
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
           val serverPrintRelaseFragment =ServerPrintRelaseFragment()
            serverPrintRelaseFragment.getjobListStatus();

        }
    }





}

//https://www.youtube.com/watch?v=vPLKNsQEAEc
//https://www.youtube.com/watch?v=nC9E9dvw2eY
