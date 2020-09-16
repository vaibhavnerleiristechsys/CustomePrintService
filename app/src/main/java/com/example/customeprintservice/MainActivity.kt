package com.example.customeprintservice

import android.content.BroadcastReceiver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.customeprintservice.jipp.AttributesUtils
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URI


class MainActivity : AppCompatActivity() {

    var receiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getTextImageFromOtherApp()
        val uri = URI.create(edtUrlInputtext.text.toString())
        val attributesUtils = AttributesUtils()

        btnGetAttributes.setOnClickListener {

            val st: String = attributesUtils.getAttributes(uri,this@MainActivity)
            Toast.makeText(this@MainActivity, st, Toast.LENGTH_LONG).show()
            Log.i("printer", "----->$st")
            broadcastIntent(it)
        }

    }

    fun broadcastIntent(view: View?) {
        Log.i("printer","In broadcastintent fun--")
        val intent = Intent()
        intent.action = "com.example.CUSTOM_INTENT"
        sendBroadcast(intent)
    }

    private fun getTextImageFromOtherApp() {
        when (intent?.action) {
            Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent)
                } else if (intent.type?.startsWith("image/") == true) {
                    handleSendImage(intent)
                }
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {
//            txtText.text = intent.getStringExtra("android.intent.extra.TEXT").toString()
        }
    }

    private fun handleSendImage(intent: Intent) {
        val imageUri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri?
        if (imageUri != null) {
//            imgView.setImageURI(imageUri)
        } else {
            Toast.makeText(this, "Error Occured, URI is invalid", Toast.LENGTH_LONG).show()
        }
    }

}