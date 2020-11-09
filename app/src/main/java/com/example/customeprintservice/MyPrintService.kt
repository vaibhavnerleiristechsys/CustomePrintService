package com.example.customeprintservice

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
import java.io.File
import java.io.FileInputStream
import java.net.URI
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPrintJobQueued(printJob: PrintJob?) {
        Log.i(TAG, "on Print Job Queued")

        val jobInfo = printJob?.info

        Log.i(TAG, "jobInfo==>${jobInfo}")
        val printerId = jobInfo?.printerId
        val localId = printerId?.localId
        val finalUrl =
            "http" + "://" + hashMap.get(printerId)?.printerHost + ":" + hashMap.get(printerId)?.printerPort + "/ipp/print"

//        doAsync {
//            val att: List<String> = PrintUtils().getPrinterSupportedFormats(
//                URI.create(finalUrl),
//                applicationContext
//            )
//            Log.i(TAG, "Printer supported format==>$att")
//        }

        val file = printJob?.document?.data

        val fileDescriptor = printJob?.document?.data?.fileDescriptor
        val input = FileInputStream(fileDescriptor)
        val fd = input.fd
        val method = fd.javaClass.getMethod("getInt$")
        val fdId = method.invoke(fd)
        val path = Paths.get("/proc/self/fd/$fdId")
        val filePath = Files.readSymbolicLink(path)
        Log.d(TAG, "filePath===========>:$filePath")

        try {
//            val read: Int
//            val bytes = ByteArray(4096)
//            val input = FileInputStream(file?.fileDescriptor)
//            val outputFile = File.createTempFile("image","temp",
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))
//            val tempFileName = outputFile.absolutePath
//            val output = FileOutputStream(tempFileName)
//            read = input.read(bytes)
//            while (read != -1) {
//                output.write(bytes, 0, read)
//            }
//            try {
//                if (input != null) {
//                    input.close()
//                    output.close()
//                }
//            } catch (e: Exception) {
//                Log.i(TAG,"e=>${e.message}")
//            }
//            Log.i(TAG, "get file path==${tempFileName}")
        val file =
            File("/storage/emulated/0/WhatsApp/Media/WhatsApp Images/IMG-20201109-WA0003.jpg")
            printUtils.print(URI.create(finalUrl), file, applicationContext, ".jpg")

        } catch (ex: Exception) {
            Log.i(TAG, "exception $ex")
        }
    }

}