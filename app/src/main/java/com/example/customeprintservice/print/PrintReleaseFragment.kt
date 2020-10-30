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
import com.example.customeprintservice.printjobstatus.PrintJobStatuses
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import com.example.customeprintservice.room.SelectedFile
import com.example.customeprintservice.utils.JwtDecode
import com.example.customeprintservice.utils.PermissionHelper
import com.example.customeprintservice.utils.Permissions
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

    @SuppressLint("CheckResult")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        initConfig()

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
        validateToken()
        /**
         * Print Job Status Web Service
         */
        PrintJobStatuses().getPrintJobStatuses(
            requireContext(),
            decodeJWT(),
            SignInCompanyPrefs.getIdpType(requireContext()).toString(),
            SignInCompanyPrefs.getIdpName(requireContext()).toString()
        )

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

    private fun saveSelectFileInDb(fileList: List<SelectedFile>): Observable<Unit> {
        return Observable.create {
            it.onNext(app.dbInstance().selectedFileDao().save(fileList))
            it.onError(Throwable())
            it.onComplete()

        }
    }

    private fun fetchList(): Observable<SelectedFile> {
        return Observable.create {
            val list = app.dbInstance().selectedFileDao().loadAll().forEach {
                it.fileName
            }
            Log.i("printer", "list fetch=>${list}")
        }
    }


    private fun decodeJWT(): String {
        var userName: String? = null
        try {
            val mapper = jacksonObjectMapper()
            val decoded: DecodedJWTResponse = mapper.readValue<DecodedJWTResponse>(
                LoginPrefs.getOCTAToken(requireContext())?.let { JwtDecode.decoded(it) }!!
            )
            Log.i("printer", "decode JWT Token=>${decoded}")
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