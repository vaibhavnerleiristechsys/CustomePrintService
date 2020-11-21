package com.example.customeprintservice

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.print.PrintAttributes
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
import java.util.function.Consumer

class PrinterLogicPrintService : PrintService() {
    private val builder: PrinterInfo? = null
    private var mHandler: Handler? = null

    companion object {
        const val MSG_HANDLE_PRINT_JOB = 3
        private const val TAG = "ThermalPrintService"
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun unbindService(conn: ServiceConnection) {
        super.unbindService(conn)
    }

    override fun onConnected() {
        Log.i(TAG, "#onConnected()")
        mHandler = PrintHandler(mainLooper)
    }

    override fun onDisconnected() {
        super.onDisconnected()
        Log.i(TAG, "#onDisConnected()")
        stopSelf()
    }

    override fun onCreatePrinterDiscoverySession(): PrinterDiscoverySession? {
        return ThermalPrinterDiscoverySession(builder, applicationContext, this)
    }

    override fun onRequestCancelPrintJob(printJob: PrintJob) {
        Log.i(TAG, "#onRequestCancelPrintJob() printJobId: " + printJob.id)
        if (mHandler!!.hasMessages(MSG_HANDLE_PRINT_JOB)) {
            mHandler!!.removeMessages(MSG_HANDLE_PRINT_JOB)
            if (printJob.isQueued || printJob.isStarted) {
                printJob.cancel()
            }
        } else {
            if (printJob.isQueued || printJob.isStarted) {
                printJob.cancel()
            }
        }
    }

    override fun onPrintJobQueued(printJob: PrintJob) {
        val message =
            mHandler!!.obtainMessage(MSG_HANDLE_PRINT_JOB, printJob)
        mHandler!!.sendMessageDelayed(message, 0)
    }

    private fun handleHandleQueuedPrintJob(printJob: PrintJob) {
        if (printJob.isQueued) {
            printJob.start()
        }
        val printerId = printJob.info.printerId
        val printerHashmap = PrinterHashmap()
        val finalUrl = "http" + "://" + printerHashmap.hashMap[printerId]!!
            .printerHost + ":" + printerHashmap.hashMap[printerId]!!.printerPort + "/ipp/print"
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
            val printUtils = PrintUtils()
            printUtils.print(URI.create(finalUrl), file, applicationContext, "")


            printJob.complete()

        } catch (ioe: IOException) {
        }
    }

    private inner class PrintHandler(looper: Looper?) : Handler(looper!!) {
        override fun handleMessage(message: Message) {
            when (message.what) {
                Companion.MSG_HANDLE_PRINT_JOB -> {
                    val printJob = message.obj as PrintJob
                    handleHandleQueuedPrintJob(printJob)
                }
            }
        }

    }
}

internal class ThermalPrinterDiscoverySession(
    printerInfo: PrinterInfo?,
    context: Context?,
    printService: PrinterLogicPrintService
) : PrinterDiscoverySession() {
    var thermalPrintService: PrinterLogicPrintService
    private var printerInfo: PrinterInfo? = null
    private var appContext: Context? = null


    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onStartPrinterDiscovery(priorityList: List<PrinterId>) {
        Log.d("customprintservices", "onStartPrinterDiscovery")
        val printUtils = PrintUtils()
        printUtils.setContextAndInitializeJMDNS(appContext)
        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val printers: MutableList<PrinterInfo?> = ArrayList()
        val printerId = arrayOfNulls<PrinterId>(1)
        val printerList = PrinterList()
        printerList.printerList.forEach(Consumer { p: PrinterModel ->
            printerId[0] = thermalPrintService.generatePrinterId(p.printerHost.toString())
            val builder = PrinterInfo.Builder(
                thermalPrintService.generatePrinterId(p.printerHost.toString()),
                p.serviceName, PrinterInfo.STATUS_IDLE
            ).build()
            val capabilities = PrinterCapabilitiesInfo.Builder(printerId[0]!!)
                .addMediaSize(PrintAttributes.MediaSize.ISO_A5, true)
                .addResolution(PrintAttributes.Resolution("1234", "Default", 200, 200), true)
                .setColorModes(
                    PrintAttributes.COLOR_MODE_MONOCHROME,
                    PrintAttributes.COLOR_MODE_MONOCHROME
                )
                .build()
            printerInfo = PrinterInfo.Builder(builder)
                .setCapabilities(capabilities)
                .build()
            printers.add(printerInfo)
            addPrinters(printers)
            val printerHashmap = PrinterHashmap()
            val hashMap: HashMap<PrinterId?, PrinterModel?> = HashMap()
            hashMap[printerId[0]] = p
            printerHashmap.hashMap = hashMap
        })
    }

    override fun onStopPrinterDiscovery() {}
    override fun onValidatePrinters(printerIds: List<PrinterId>) {}
    override fun onStartPrinterStateTracking(printerId: PrinterId) {}
    override fun onStopPrinterStateTracking(printerId: PrinterId) {}
    override fun onDestroy() {}

    init {
        appContext = context
        thermalPrintService = printService
    }
}

internal class PrinterHashmap {
    var hashMap: HashMap<PrinterId?, PrinterModel?>
        get() = Companion.hashMap
        set(hashMap) {
            Companion.hashMap = hashMap
        }
    companion object {
        private var hashMap: HashMap<PrinterId?, PrinterModel?> = HashMap()
    }
}