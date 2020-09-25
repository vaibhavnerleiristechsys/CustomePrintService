package com.example.customeprintservice.jipp

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.customeprintservice.R
import kotlinx.android.synthetic.main.activity_print.*
import java.io.File
import java.net.URI

class PrintActivity : AppCompatActivity() {

    val printUtils = PrintUtils()
    var bundle: Bundle = Bundle()
    var attributesUtils = AttributesUtils()

    var uri: URI? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print)
        val actionBar = supportActionBar
        actionBar?.title = "Print"
        actionBar?.setDisplayHomeAsUpEnabled(true)

        bundle = intent.extras!!

        if (bundle.getString("selectedFile") != null) {

            val selectedFile: String? = bundle.getString("selectedFile")
            val ipAddress: String? = bundle.getString("ipAddress")
            val printerName: String? = bundle.getString("printerName")
            val formatSupported: String? = bundle.getString("formatSupported")


            txtDignosticInfo.text = bundle.getString("printerAttribute")
            txtPrinterActivitySelectedDocument.text =
                "Selected Document - ${selectedFile.toString()}"
            txtPrinterActivityPrinterName.text = "Printer Name - ${printerName.toString()}"
            txtPrinterActivityFormatSupported.text =
                "format Supported -${formatSupported.toString()}"

        }
        uri =
            URI.create("http://" + bundle.getString("ipAddress") + ":${bundle.getString("printerPort")}" + "/" + "ipp/print")
        Log.i("printer", "uri1---->$uri")

        btnPrint.setOnClickListener {

            /* val file: File = File(bundle.getString("selectedFile")!!)  */


            val uri1 = Uri.parse(bundle.getString("selectedFile"))
            val file: File = File(bundle.getString("selectedFile")!!)
            val inputFile = File(file.absolutePath)

            val fileName: String = inputFile.name
            var format: String? = null

            if (fileName.contains(".")) {
                format = PrintUtils.extensionTypes[fileName.substring(fileName.lastIndexOf(".") + 1)
                    .toLowerCase()]
                Log.i("printer", "format--->$format")
            }


            printUtils.print(uri, file, this@PrintActivity)
        }

        /*val att: List<String> =
          attributesUtils.getAttributesForPrintUtils(uri, this@PrintActivity)
      try {
          Thread.sleep(1000)
      } catch (e: InterruptedException) {
      }
      Log.i("printer", "att lst--->${att.toString()}")*/

        /*if(att.contains(format?.trim())){
            txtPrinterActivityIsPrintSupported.text = "Is Print format support - True"
            Toast.makeText(this@PrintActivity,"File is Supported to Print",Toast.LENGTH_LONG).show()
        }else{
            txtPrinterActivityIsPrintSupported.text = "Is Print format support - False"
            Toast.makeText(this@PrintActivity,"File is Not Supported ",Toast.LENGTH_LONG).show()
        }*/


        val filter = IntentFilter()
        filter.addAction("com.example.PRINT_RESPONSE")
        //filter.addAction("com.example.PRINT_SUPPORT_FORMATS")
        val receiver = broadcastReceiver
        registerReceiver(receiver, filter)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            try {
                var printResponse: String = ""
                if (intent.getStringExtra("printResponse") != null) {
                    printResponse = intent.getStringExtra("printResponse").toString()
                    txtPrinterResponse.text = "Print Response - $printResponse"
                }


                var printerSupportedFormats: String = ""

                if (intent.getStringExtra("printerSupportedFormats") != null) {
                    printerSupportedFormats =
                        intent.getStringExtra("printerSupportedFormats").toString()
                    txtPrinterActivityFormatSupported.text =
                        "Priter Supported Format - $printerSupportedFormats"
                }

                var getPrinterAttributes: String = ""
                if (intent.getStringExtra("getPrinterAttributes") != null) {
                    getPrinterAttributes = intent.getStringExtra("getPrinterAttributes").toString()
                    txtDignosticInfo.text = "Get Attributes - $getPrinterAttributes"
                }

                var exception: String = ""
                if (intent.getStringExtra("exception") != null) {
                    exception = intent.getStringExtra("exception").toString()
                    txtDignosticInfo.text = "Exception Occured - $exception"
                }

                var fileNotSupported :String =""
                if (intent.getStringExtra("fileNotSupported") != null) {
                    fileNotSupported = intent.getStringExtra("fileNotSupported").toString()
                    Toast.makeText(this@PrintActivity,fileNotSupported,Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                txtDignosticInfo.text = e.toString()
            }
        }
    }

}