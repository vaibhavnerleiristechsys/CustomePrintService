package com.example.customeprintservice.jipp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.customeprintservice.R
import kotlinx.android.synthetic.main.activity_print.*
import java.io.File
import java.net.URI

class PrintActivity : AppCompatActivity() {

    val printUtils = PrintUtils()
    var bundle:Bundle = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print)
        val actionBar = supportActionBar
        actionBar?.title = "Print"
        actionBar?.setDisplayHomeAsUpEnabled(true)

        bundle = intent.extras!!

        if(bundle.getString("selectedFile") != null ){
            val selectedFile: String? = bundle.getString("selectedFile")
            val ipAddress: String? = bundle.getString("ipAddress")
            Log.i("printer", "selected file-->$selectedFile ipAddress-->-$ipAddress")
        }
        btnPrint.setOnClickListener {

            val uri = URI.create("http://"+bundle.getString("ipAddress")+"/"+"ipp/print")
//            val aUri = "http://$uri/ipp/print"
            val uri1 = Uri.parse(bundle.getString("selectedFile"))
            Log.i("printer","uri1---->"+uri)

            val file: File = File(bundle.getString("selectedFile")!!)

            printUtils.print(uri, file, this@PrintActivity)
        }

        val filter = IntentFilter()
        filter.addAction("com.example.CUSTOM_INTENT")
        val receiver = broadcastReceiver
        registerReceiver(receiver, filter)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val ippPacket: String = intent.getStringExtra("getMessage").toString()
            Log.i("printer", "msg---->$ippPacket")

            try {
               txtDignosticInfo.text = ippPacket
            } catch (e: Exception) {
            }
        }
    }

}