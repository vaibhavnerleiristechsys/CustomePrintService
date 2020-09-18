package com.example.customeprintservice.jipp

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.customeprintservice.R
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileInputStream
import java.net.URI


class MainActivity : AppCompatActivity() {
    val printUtils = PrintUtils()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        getTextImageFromOtherApp()

        val attributesUtils = AttributesUtils()

        btnGetAttributes.setOnClickListener {

            val uri = URI.create(edtUrlInputtext.text.toString())
            val st: String = attributesUtils.getAttributes(uri, this@MainActivity)
            Log.i("printer", "----->$st")
            txtRequestAttribute.text = "Request-->$st"
        }

        btnPrint.setOnClickListener {

            val mRequestFileIntent = Intent(Intent.ACTION_GET_CONTENT)
            mRequestFileIntent.type = "*/*"
            startActivityForResult(mRequestFileIntent, 1)
        }

        val filter = IntentFilter()
        filter.addAction("com.example.CUSTOM_INTENT")
        val receiver = broadcastReceiver
        registerReceiver(receiver, filter)

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

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            val uri: Uri = data.data!!
            val file = File(uri.path)
            var fileInputStream:FileInputStream = FileInputStream(uri.path)

            val uri1 = URI.create(edtUrlInputtext.text.toString())
            printUtils.print(uri1, file)

            Log.i("printer", "file choosed-->" + uri.path)
        }
    }

    private fun getRealPathFromURI(contentURI: Uri): String? {
        val result: String?
        val cursor = contentResolver.query(contentURI, null, null, null, null)
        if (cursor == null) { // Source is Dropbox or other similar local file path
            result = contentURI.path
        } else {
            cursor.moveToFirst()
            val idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            result = cursor.getString(idx)
            cursor.close()
        }
        return result
    }

}