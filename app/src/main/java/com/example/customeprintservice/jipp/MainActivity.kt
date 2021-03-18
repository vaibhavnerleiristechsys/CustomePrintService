package com.example.customeprintservice.jipp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.R
import com.example.customeprintservice.adapter.SelectedFileListMainActivityAdapter
import com.example.customeprintservice.utils.PermissionHelper
import com.example.customeprintservice.utils.Permissions
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {

    private val bundle = Bundle()
    private var isFileSelected: Boolean = false
    private var permissionsHelper: PermissionHelper? = null
    private var list = ArrayList<String>()
    private val rxPermissions = RxPermissions(this)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getTextImageFromOtherApp()

        val actionBar = supportActionBar
        actionBar?.title = "IPP Print Demo"

        checkPermissions()

        btnSelectDocument.setOnClickListener {
            if (Permissions().checkAndRequestPermissions(this@MainActivity)) {
                val i = Intent(
                    Intent.ACTION_GET_CONTENT,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                i.type = "*/*"
                startActivityForResult(i, 1)
          //      PrintUtils().setContextAndInitializeJMDNS(this@MainActivity)
//            val mRequestFileIntent = Intent(Intent.ACTION_GET_CONTENT)
//            startActivityForResult(mRequestFileIntent, 1)
            } else {
                Toast.makeText(this@MainActivity, "Please accept Permissions", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        btnNext.setOnClickListener {

            if (isFileSelected && list.size > 0) {
                val intent = Intent(this@MainActivity, PrinterDiscoveryActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            } else {
                Toast.makeText(this@MainActivity, "Select the Document", Toast.LENGTH_SHORT).show()
            }

//            rxPermissions
//                .request(Manifest.permission.CAMERA)
//                .subscribe { granted ->
//                    if (granted) {
//                        val intent = Intent(this@MainActivity, QRCodeScanActivity::class.java)
//                        startActivity(intent)
//                    } else {
//                        toast("Camera Permission needed")
//                    }
//                }
        }
    }

    private fun checkPermissions() {
        permissionsHelper = PermissionHelper()
        permissionsHelper!!.checkAndRequestPermissions(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsHelper!!.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            val uri: Uri = data?.data!!
            val realPath = FileUtils.getPath(this@MainActivity, uri)
            val file: File = File(realPath)
            if (!list.contains(realPath)) {
                list.add(realPath)
            } else {
                Toast.makeText(this@MainActivity, "File is already selected", Toast.LENGTH_SHORT)
                    .show()
            }
            listUpdate(list)
            isFileSelected = true
            bundle.putStringArrayList("selectedFileList", list)
            Log.i("printer", "file choosed-->$file")
            Log.i("printer", "list of Files-->$list")
        }
    }

    @SuppressLint("WrongConstant")
    private fun listUpdate(list: ArrayList<String>) {
        val recyclerViewSelectedFileLstMainActivity =
            findViewById<RecyclerView>(R.id.recyclerViewSelectedFileListMainActivity)
        recyclerViewSelectedFileLstMainActivity.layoutManager =
            LinearLayoutManager(
                this@MainActivity,
                LinearLayout.VERTICAL,
                false
            )

        val adapter = SelectedFileListMainActivityAdapter(
            this@MainActivity,
            list
        )
        recyclerViewSelectedFileLstMainActivity.adapter = adapter
    }

    private fun getTextImageFromOtherApp() {
        when (intent?.action) {
            Intent.ACTION_SEND_MULTIPLE -> {
                handleSendMultipleImages(intent)
                if (intent.type?.startsWith("*/*") == true) {
                    Log.i("printer", "in action send text image")
                }
            }
        }
    }


    private fun handleSendImage(intent: Intent) {
        val imageUri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri?
        if (imageUri != null) {
            val realPath = FileUtils.getPath(this, imageUri)
            isFileSelected = true
            bundle.putString("selectedFile", realPath)

            if (!list.contains(realPath)) {
                list.add(realPath)
            } else {
                Toast.makeText(this@MainActivity, "File is already selected", Toast.LENGTH_SHORT)
                    .show()
            }
            listUpdate(list)
            bundle.putStringArrayList("selectedFileList", list)
        } else {
            Toast.makeText(this, "Error Occurred, URI is invalid", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleSendMultipleImages(intent: Intent) {
        intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM).let {
            it?.forEach {
                val realPath = FileUtils.getPath(this, it)
                Log.i("printer", "realpath==>$realPath")
                if (!list.contains(realPath)) {
                    list.add(realPath)
                } else {
                    Toast.makeText(this@MainActivity, "File is already selected", Toast.LENGTH_SHORT).show()
                }
            }
        }
        isFileSelected = true
        listUpdate(list)
        bundle.putStringArrayList("selectedFileList", list)

    }
}