package com.example.customeprintservice.print

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.R
import com.example.customeprintservice.adapter.FragmentSelectedFileListAdapter
import com.example.customeprintservice.jipp.FileUtils
import com.example.customeprintservice.jipp.PrintUtils
import com.example.customeprintservice.model.FileAttributes
import com.example.customeprintservice.utils.PermissionHelper
import com.example.customeprintservice.utils.Permissions
import kotlinx.android.synthetic.main.fragment_print_release.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class PrintReleaseFragment : Fragment() {

    private var permissionsHelper: PermissionHelper? = null
    private val bundle = Bundle()
    private var isFileSelected: Boolean = false
    private var list = ArrayList<FileAttributes>()
//    private val rxPermissions = RxPermissions(this)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_print_release, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermissions()

        btnFragmentSelectDoc.setOnClickListener {
            if (Permissions().checkAndRequestPermissions(context as Activity)) {
                val i = Intent(
                    Intent.ACTION_GET_CONTENT,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                i.type = "*/*"
                startActivityForResult(i, 1)
                PrintUtils().setContextAndInitializeJMDNS(context as Activity)
            } else {
                Toast.makeText(context as Activity, "Please accept Permissions", Toast.LENGTH_SHORT)
                    .show()
            }
        }
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

    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == AppCompatActivity.RESULT_OK) {
            val uri: Uri = data?.data!!
            val realPath = FileUtils.getPath(context as Activity, uri)
            val file: File = File(realPath)

            val fileAttribute = FileAttributes()
            fileAttribute.fileName = file.name
            fileAttribute.fileRealPath = realPath
            fileAttribute.fileSize = file.length()/1024
            val c = Calendar.getInstance()
            val df = SimpleDateFormat("dd-MM HH:mm ")
            val formattedDate: String = df.format(c.time)
            fileAttribute.fileSelectedDate = formattedDate

            list.add(fileAttribute)
            listUpdate(list)
            isFileSelected = true
//            bundle.putStringArrayList("selectedFileList", list)
            Log.i("printer", "file choosed-->$file")
            Log.i("printer", "list of Files-->$list")
        }
    }

    @SuppressLint("WrongConstant")
    private fun listUpdate(list: ArrayList<FileAttributes>) {
        val recyclerViewDocumentList =
            view?.findViewById<RecyclerView>(R.id.recyclerViewDocumentList)
        recyclerViewDocumentList?.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayout.VERTICAL,
                false
            )

        val adapter = FragmentSelectedFileListAdapter(
            context as Activity,
            list
        )
        recyclerViewDocumentList?.adapter = adapter
    }


}

//https://www.youtube.com/watch?v=vPLKNsQEAEc
//https://www.youtube.com/watch?v=nC9E9dvw2eY