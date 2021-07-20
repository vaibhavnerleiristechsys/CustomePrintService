package com.printerlogic.printerlogic.jipp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val ippPacket: String = intent.getStringExtra("getMessage").toString()
        Log.i("printer", "msg---->$ippPacket")
        var printResponseStatus: String = ""
        if (intent.getStringExtra("getPrintResponse") != null) {
            printResponseStatus = intent.getStringExtra("getPrintResponse").toString()
            Log.i("printer", "printResponseStatus=>$printResponseStatus")
           // Toast.makeText(context, printResponseStatus, Toast.LENGTH_LONG).show()
        }
        var printResponse: String = ""
        if (intent.getStringExtra("printResponse") != null) {
            printResponse = intent.getStringExtra("printResponse").toString()


            /*  firebaseAnalytics.setDefaultEventParameters(
                  debugString(
                      printResponse,
                      "printResponse"
                  )
              )*/
            //Toast.makeText(context,"Print Response"+printResponse,Toast.LENGTH_LONG).show()
        }

        var printerSupportedFormats: String = ""
        if (intent.getStringExtra("printerSupportedFormats") != null) {
            printerSupportedFormats =
                intent.getStringExtra("printerSupportedFormats").toString()
            //Toast.makeText(context,"printerSupportedFormats"+printerSupportedFormats,Toast.LENGTH_LONG).show()
        }

        var getPrinterAttributes: String = ""
        if (intent.getStringExtra("getPrinterAttributes") != null) {
            getPrinterAttributes = intent.getStringExtra("getPrinterAttributes").toString()
            //Toast.makeText(context,"Print getPrinterAttributes  Response"+getPrinterAttributes,Toast.LENGTH_LONG).show()
//                    firebaseAnalytics.setDefaultEventParameters(debugString(getPrinterAttributes,"attributes"))

        }

        var uri: String = ""
        if (intent.getStringExtra("finalUri") != null) {
            uri = intent.getStringExtra("finalUri").toString()
            //Toast.makeText(context,"uri:"+uri,Toast.LENGTH_LONG).show()
        }

        var exception: String = ""
        if (intent.getStringExtra("exception") != null) {
            exception = intent.getStringExtra("exception").toString()
            //Toast.makeText(context,"Print exception"+exception,Toast.LENGTH_LONG).show()
        }

        var fileNotSupported: String = ""
        if (intent.getStringExtra("fileNotSupported") != null) {
            fileNotSupported = intent.getStringExtra("fileNotSupported").toString()
            //Toast.makeText(context, fileNotSupported, Toast.LENGTH_LONG).show()

        }

        try {
            val mainActivity = MainActivity()
//            mainActivity.getInst()?.updatedUi(ippPacket)
        } catch (e: Exception) { }
    }
}