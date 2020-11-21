package com.example.customeprintservice

import android.annotation.SuppressLint
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintAttributes.Resolution
import android.print.PrinterCapabilitiesInfo
import android.print.PrinterId
import android.print.PrinterInfo
import android.printservice.PrintJob
import android.printservice.PrintService
import android.printservice.PrinterDiscoverySession
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.customeprintservice.jipp.PrintUtils
import com.example.customeprintservice.jipp.PrinterList
import com.example.customeprintservice.jipp.PrinterModel
import java.io.*
import java.net.URI
import java.util.*
import kotlin.collections.HashMap


class MyPrintService : PrintService() {

    private val TAG = this.javaClass.name
    val hashMap = HashMap<PrinterId, PrinterModel>()
    val printUtils = PrintUtils()

    override fun onCreatePrinterDiscoverySession(): PrinterDiscoverySession? {
        return abc
    }

    var abc: PrinterDiscoverySession = object : PrinterDiscoverySession() {
        override fun onValidatePrinters(printerIds: List<PrinterId>) {
            Log.d(TAG, "onValidatePrinters")
        }

        override fun onStopPrinterStateTracking(printerId: PrinterId) {
            Log.d(TAG, "onStopPrinterStateTracking")
        }

        override fun onStopPrinterDiscovery() {
            Log.d(TAG, "onStopPrinterDiscovery")
        }

        @RequiresApi(Build.VERSION_CODES.N)
        override fun onStartPrinterStateTracking(printerId: PrinterId) {
            Log.d(TAG, "onStartPrinterStateTracking")
            Log.i(TAG, "printer selected=>${printerId}")
            hashMap.forEach { t, u ->
                run {
                    if (printerId == t) {
                        Log.i(TAG, "onStartPrinterStateTracking==>${u.serviceName}")
                    }
                }
            }
        }

        override fun onStartPrinterDiscovery(priorityList: MutableList<PrinterId>) {
            Log.d(TAG, "onStartPrinterDiscovery")
            PrintUtils().setContextAndInitializeJMDNS(applicationContext)

            try {
                Thread.sleep(3000)
            } catch (e: Exception) {
                Log.i(TAG, "ex==>${e.message}")
            }
            val printers: MutableList<PrinterInfo> = ArrayList()
            var printerId: PrinterId

            PrinterList().printerList.forEach {
                Log.i(TAG, "printer list nsd utils=>${it.serviceName}")
                printerId = generatePrinterId(it.printerHost.toString())
                val builder =
                    PrinterInfo.Builder(printerId, it.serviceName, PrinterInfo.STATUS_IDLE)
                val capBuilder = PrinterCapabilitiesInfo.Builder(printerId)
                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A4, true)
                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A3, false)
                capBuilder.addResolution(
                    Resolution("resolutionId", "default resolution", 600, 600),
                    true
                )
                capBuilder.setColorModes(
                    PrintAttributes.COLOR_MODE_COLOR or PrintAttributes.COLOR_MODE_MONOCHROME,
                    PrintAttributes.COLOR_MODE_COLOR
                )

                builder.setCapabilities(capBuilder.build())
                printers.add(builder.build())
                addPrinters(printers)

                hashMap.put(printerId, it)
            }

            Log.i(TAG, "Is printer discovery started $isPrinterDiscoveryStarted")
        }

        override fun onDestroy() {
            Log.d(TAG, "onDestroy")
        }
    }

    override fun onRequestCancelPrintJob(printJob: PrintJob?) {
        Log.i(TAG, "on RequestCancelPrintJob")
    }

    @SuppressLint("SdCardPath")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPrintJobQueued(printJob: PrintJob?) {
        Log.i(TAG, "on Print Job Queued")
        val jobInfo = printJob?.info

        Log.i(TAG, "jobInfo==>${jobInfo}")
        val printerId = jobInfo?.printerId
        val finalUrl =
            "http" + "://" + hashMap.get(printerId)?.printerHost + ":" + hashMap.get(printerId)?.printerPort + "/ipp/print"

        if (printJob!!.isQueued) {
            printJob.start()
        }
        val info = printJob.info
        val file = File(filesDir, info.label + ".pdf")


        var `in`: InputStream? = null
        var out: FileOutputStream? = null

        try {
            `in` = FileInputStream(printJob.document.data!!.fileDescriptor)
            out = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var read: Int
            while (`in`.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            `in`.close()
            out.flush()
            out.close()

            printUtils.print(URI.create(finalUrl), file, applicationContext, "")
            val sb = StringBuilder()
            val fisTest = FileInputStream(file)
            var readByte = 0
            while (readByte != -1) {
                readByte = fisTest.read()
                val readChar = readByte.toChar()
                sb.append(readChar)
            }
            fisTest.close()
            Log.i("print=====>", sb.toString())
        } catch (ioe: IOException) {
        }

//        if (printJob.isQueued) {
//            printJob.start()
//        }
//        val jobInfo = printJob.info
//
//        Log.i(TAG, "jobInfo==>${jobInfo}")
//        val printerId = jobInfo.printerId
//        val finalUrl =
//            "http" + "://" + hashMap.get(printerId)?.printerHost + ":" + hashMap.get(printerId)?.printerPort + "/ipp/print"
//
//        val fileDescriptor = printJob.document.data?.fileDescriptor
//
//        val printerdoc = printJob.document.info
//
//
//        val outputFile = FileDescriptorConverter().convertFile(fileDescriptor, this@MyPrintService)
//        Log.i("printer", "outputFile==>${outputFile.path}")
//        Log.i("printer", "file size==>${outputFile.length()}")


//        printUtils.print(URI.create(finalUrl), outputFile, applicationContext, "")

    }


}