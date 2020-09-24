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

    var uri:URI? = null
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

            Log.i("printer", "printer Format Support in print activity-->$formatSupported")
            Log.i("printer", "printer attribute in print activity->"+bundle.getString("printerAttribute"))
        }
        uri = URI.create("http://" + bundle.getString("ipAddress") + "/" + "ipp/print")
        Log.i("printer", "uri1---->$uri")

        btnPrint.setOnClickListener {

           /* val file: File = File(bundle.getString("selectedFile")!!)  */


            val uri1 = Uri.parse(bundle.getString("selectedFile"))
            val file: File = File(bundle.getString("selectedFile")!!)
            val inputFile = File(file.absolutePath)

            val fileName: String = inputFile.name
            var format: String? =  null

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
        val receiver = broadcastReceiver
        registerReceiver(receiver, filter)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            try
            {
                val ippPacket: String = intent.getStringExtra("getMessage").toString()
                Log.i("printer", "msg---->$ippPacket")
                txtPrinterResponse.text = ippPacket
            }
            catch (e: Exception){
                txtDignosticInfo.text = e.toString();
            }
        }
    }

}