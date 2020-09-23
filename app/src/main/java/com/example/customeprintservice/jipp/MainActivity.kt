package com.example.customeprintservice.jipp

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.customeprintservice.R
import com.example.customeprintservice.utils.Permissions
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {


    val bundle = Bundle()
    val attributesUtils = AttributesUtils()
    private val PERMISSION_READ_EXTERNAL = 1
    var isFileSelected: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        getTextImageFromOtherApp()

        val actionBar = supportActionBar
        actionBar?.title = "IPP Print Demo"


        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this@MainActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1
                )
            } else {
                ActivityCompat.requestPermissions(
                    this@MainActivity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1
                )
            }
        }

        btnSelectDocument.setOnClickListener {
            if (Permissions().checkAndRequestPermissions(this@MainActivity)) {
                val i = Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                i.type = "*/*"
                startActivityForResult(i, 1)
                PrintUtils().setContextAndInitializeJMDNS(this@MainActivity)
//            val mRequestFileIntent = Intent(Intent.ACTION_GET_CONTENT)
//            startActivityForResult(mRequestFileIntent, 1)
            } else {
                Toast.makeText(this@MainActivity, "Please accept Permissions", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        btnNext.setOnClickListener {

            startActivity(intent)
            if (isFileSelected) {
                val intent = Intent(this@MainActivity, PrinterDiscoveryActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            } else {
                Toast.makeText(this@MainActivity, "Select Document", Toast.LENGTH_LONG).show()
            }
        }


//        btnGetAttributes.setOnClickListener {
//            if (Permissions().checkAndRequestPermissions(this)) {
//                val uri = URI.create(edtUrlInputtext.text.toString())
//                val st: String = attributesUtils.getAttributes(uri, this@MainActivity)
//                Log.i("printer", "----->$st")
//                txtRequestAttribute.text = "Request-->$st"
//            } else {
//                Toast.makeText(this@MainActivity, "Please accept Permissions", Toast.LENGTH_SHORT)
//                    .show()
//            }
//        }




    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if ((ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) ==
                                PackageManager.PERMISSION_GRANTED)
                    ) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            val uri: Uri = data?.data!!
            val realPath = FileUtils.getPath(this, uri)
            val file: File = File(realPath)

            txtPath.text = uri.path
            isFileSelected = true
            bundle.putString("selectedFile", realPath)

//            val uri1 = URI.create(edtUrlInputtext.text.toString())
//            printUtils.print(uri1, file, this@MainActivity)

            Log.i("printer", "file choosed-->$file")
        }
    }

}