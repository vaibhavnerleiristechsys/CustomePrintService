package com.example.customeprintservice

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.customeprintservice.jipp.GetAttributes
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.doAsync
import java.net.URI


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getTextImageFromOtherApp()
        val uri = URI.create(edtUrlInputtext.text.toString())

        btnGetAttributes.setOnClickListener {
            doAsync {

                val getAttributes = GetAttributes()
                getAttributes.getAttributes(uri, this@MainActivity)
            }

        }

        val m: MyPrintService = MyPrintService()
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