package com.example.customeprintservice.print

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.PrintService
import com.example.customeprintservice.R
import com.example.customeprintservice.adapter.FragmentSelectedFileListAdapter
import com.example.customeprintservice.jipp.FileUtils
import com.example.customeprintservice.jipp.PrinterDiscoveryActivity
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
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_print_release.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
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
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
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

        btnRelease.setOnClickListener {
            ProgressDialog.showLoadingDialog(requireContext(), "Released Job")
            releaseJob()
        }

        /**
         * Print Job status cancel
         */

        btnDeleteJobs.setOnClickListener {
            ProgressDialog.showLoadingDialog(requireContext(), "Cancel Job")
            cancelJob()
        }

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

    fun cancelJob() {
        val BASE_URL = "https://gw.app.printercloud.com/devncookta/pq/api/job-statuses/cancel/"
        val apiService = RetrofitClient(requireContext())
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val jobStatusCancel = CancelJobRequest()
        val deleteJobs = ArrayList<DeleteJobsItem>()
        releaseJobCheckedList.forEach {
            val deleteJobsItem = DeleteJobsItem()
            deleteJobsItem.jobNum = it.jobNum
            deleteJobsItem.jobType = it.jobType
            deleteJobsItem.queueId = it.queueId
            deleteJobsItem.userName = it.userName
            deleteJobsItem.workstationId = it.workStationId
            deleteJobs.add(deleteJobsItem)
        }
        jobStatusCancel.deleteJobs = deleteJobs
        val call = apiService.jobStatusCancel(
            "Bearer " + LoginPrefs.getOCTAToken(requireContext()),
            decodeJWT(),
            SignInCompanyPrefs.getIdpType(requireContext()).toString(),
            SignInCompanyPrefs.getIdpName(requireContext()).toString(),
            jobStatusCancel
        )

        call.enqueue(object : Callback<CancelJobResponse> {
            override fun onResponse(
                call: Call<CancelJobResponse>,
                response: Response<CancelJobResponse>
            ) {
                ProgressDialog.cancelLoading()
                if (response.code() == 200) {
                    val resp = response.body().toString()
                    Log.i("printer", "response cancel job==>${resp}")
                    ProgressDialog.showLoadingDialog(requireContext(), "Refreshing Job List")
                    getJobStatuses(
                        requireContext(),
                        decodeJWT(),
                        SignInCompanyPrefs.getIdpType(requireContext()).toString(),
                        SignInCompanyPrefs.getIdpName(requireContext()).toString()
                    )
                }
            }

            override fun onFailure(call: Call<CancelJobResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Toast.makeText(requireContext(), "Validation Failed", Toast.LENGTH_SHORT).show()
                Log.i("printer", "Error response cancel job==>${t.message}")
            }
        })
    }

    fun releaseJob() {
        val BASE_URL = "https://gw.app.printercloud.com/devncookta/pq/api/job-statuses/release/"
        val apiService = RetrofitClient(requireContext())
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val releaseJobRequest = ReleaseJobRequest()
        val releaseJobs = ArrayList<ReleaseJobsItem>()
        releaseJobCheckedList.forEach {
            val releaseJobsItem = ReleaseJobsItem()
            releaseJobsItem.jobNum = it.jobNum
            releaseJobsItem.jobType = it.jobType
            releaseJobsItem.queueId = it.queueId
            releaseJobsItem.userName = it.userName
            releaseJobsItem.workstationId = it.workStationId
            releaseJobs.add(releaseJobsItem)
        }
        releaseJobRequest.releaseJobs = releaseJobs

        val call = apiService.releaseJob(
            releaseJobRequest, "Bearer " + LoginPrefs.getOCTAToken(requireContext()),
            decodeJWT(),
            SignInCompanyPrefs.getIdpType(requireContext()).toString(),
            SignInCompanyPrefs.getIdpName(requireContext()).toString()
        )

        call.enqueue(object : Callback<ReleaseJobResponse> {
            override fun onResponse(
                call: Call<ReleaseJobResponse>,
                response: Response<ReleaseJobResponse>
            ) {
                ProgressDialog.cancelLoading()
                if (response.code() == 200) {
                    val response = response.body().toString()
                    Log.i("printer", "response release job==>${response}")
                    ProgressDialog.showLoadingDialog(requireContext(), "Refreshing Job List")
                    getJobStatuses(
                        requireContext(),
                        decodeJWT(),
                        SignInCompanyPrefs.getIdpType(requireContext()).toString(),
                        SignInCompanyPrefs.getIdpName(requireContext()).toString()
                    )
                }
            }

            override fun onFailure(call: Call<ReleaseJobResponse>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Toast.makeText(requireContext(), "Validation Failed", Toast.LENGTH_SHORT).show()
                Log.i("printer", "Error response release job==>${t.message}")
            }

        })
    }

    fun getJobStatuses(context: Context, userName: String, idpType: String, idpName: String) {

        val BASE_URL = "https://gw.app.printercloud.com/devncookta/pq/api/job-statuses/"
        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call = apiService.getPrintJobStatuses(
            "Bearer " + LoginPrefs.getOCTAToken(context),
            userName,
            idpType,
            idpName
        )
        call.enqueue(object : Callback<GetJobStatusesResponse> {
            override fun onResponse(
                call: Call<GetJobStatusesResponse>,
                response: Response<GetJobStatusesResponse>
            ) {
                ProgressDialog.cancelLoading()
                val getJobStatusesResponse = response.body()?.printQueueJobStatus
                if (getJobStatusesResponse?.size == 0) {
                    Toast.makeText(requireContext(), "Empty list..No Job Hold", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val parseList: List<PrintQueueJobStatusItem?>? =
                        getJobStatusesResponse
                    val disposable4 = Observable.fromCallable {
                        val selectedFileList = ArrayList<SelectedFile>()
                        parseList?.forEach {
                            val selectedFile = SelectedFile()
                            selectedFile.isFromApi = true
                            selectedFile.fileName = it?.documentTitle
                            selectedFile.fileSelectedDate = it?.submittedAtRelative
                            selectedFile.filePath = it?.jobSize.toString()
                            selectedFile.jobNum = it?.jobNumber
                            selectedFile.jobType = 1
                            selectedFile.queueId = it?.printerDeviceQueueId
                            selectedFile.userName = it?.userName
                            selectedFile.workStationId = it?.workstationId
                            selectedFileList.add(selectedFile)
                        }
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

    private fun decodeJWT(): String {
        var userName: String? = null
        try {
            val mapper = jacksonObjectMapper()
            val decoded: DecodedJWTResponse = mapper.readValue<DecodedJWTResponse>(
                LoginPrefs.getOCTAToken(requireContext())?.let { JwtDecode.decoded(it) }!!
            )
            userName = decoded.user.toString()
        } catch (ex: Exception) {
            requireContext().toast("Failed to Decode Jwt Token")
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
}


//https://www.youtube.com/watch?v=vPLKNsQEAEc
//https://www.youtube.com/watch?v=nC9E9dvw2eY