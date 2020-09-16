package com.example.customeprintservice.jipp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.customeprintservice.R
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URI


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        getTextImageFromOtherApp()
        val uri = URI.create(edtUrlInputtext.text.toString())
        val attributesUtils = AttributesUtils()

        btnGetAttributes.setOnClickListener {

            val st: String = attributesUtils.getAttributes(uri, this@MainActivity)
            Log.i("printer", "----->$st")
            txtRequestAttribute.text = "Request-->$st"
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

}