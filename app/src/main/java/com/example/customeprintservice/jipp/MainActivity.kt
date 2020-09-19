package com.example.customeprintservice.jipp

import android.Manifest
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
import com.example.customeprintservice.jmdns.PrinterDiscoveryUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.net.URI


@Suppress("DEPRECATED_IDENTITY_EQUALS")
class MainActivity : AppCompatActivity() {

    val printUtils = PrintUtils()
    val attributesUtils = AttributesUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        getTextImageFromOtherApp()

        var printerDiscoveryUtils: PrinterDiscoveryUtils = PrinterDiscoveryUtils()
        printerDiscoveryUtils.printerDiscovery()

        if (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) !==
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

        val filter = IntentFilter()
        filter.addAction("com.example.CUSTOM_INTENT")
        val receiver = broadcastReceiver
        registerReceiver(receiver, filter)

    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    if ((ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ) ===
                                PackageManager.PERMISSION_GRANTED)
                    ) {
                        Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                        btnGetAttributes.setOnClickListener {

                            val uri = URI.create(edtUrlInputtext.text.toString())
                            val st: String = attributesUtils.getAttributes(uri, this@MainActivity)
                            Log.i("printer", "----->$st")
                            txtRequestAttribute.text = "Request-->$st"
                        }

                        btnPrint.setOnClickListener {
                            val i = Intent(
                                Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                            )
                            i.type = "*/*"
                            startActivityForResult(i, 1)
//            val mRequestFileIntent = Intent(Intent.ACTION_GET_CONTENT)
//            startActivityForResult(mRequestFileIntent, 1)
                        }

                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }

    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val ippPacket: String = intent.getStringExtra("getMessage").toString()
            Log.i("printer", "msg---->$ippPacket")

            try {
                txtResponseAttribute.text = ippPacket
            } catch (e: Exception) {
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == RESULT_OK) {
            val uri: Uri = data?.data!!
            val realPath = FileUtils.getPath(this, uri)
            val file: File = File(realPath)

            val uri1 = URI.create(edtUrlInputtext.text.toString())
            printUtils.print(uri1, file, this@MainActivity)

            Log.i("printer", "file choosed-->" + uri.path)
        }
    }

}