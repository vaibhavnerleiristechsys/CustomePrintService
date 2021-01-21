package com.example.customeprintservice

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.print.PrintAttributes
import android.print.PrinterCapabilitiesInfo
import android.print.PrinterId
import android.print.PrinterInfo
import android.printservice.PrintJob
import android.printservice.PrintService
import android.printservice.PrinterDiscoverySession
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.customeprintservice.jipp.PrintRenderUtils
import com.example.customeprintservice.jipp.PrintUtils
import com.example.customeprintservice.jipp.PrinterList
import com.example.customeprintservice.jipp.PrinterModel
import java.io.*
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList


class PrinterLogicPrintService : PrintService() {
    private val builder: PrinterInfo? = null

    companion object {
        const val MSG_HANDLE_PRINT_JOB = 3
        private const val TAG = "PrintService"
    }

    override fun onConnected() {
        Log.i(TAG, "#onConnected()")

        val permissionWrite = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val permissionRead = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

       if (permissionWrite != PackageManager.PERMISSION_GRANTED
            || permissionRead != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "Permission to record denied")
            Toast.makeText(this, "Please login to Printerlogic app", Toast.LENGTH_LONG).show()
        }

    }

    override fun onDisconnected() {
        super.onDisconnected()
        Log.i(TAG, "#onDisConnected()")
        stopSelf()
    }

    override fun onCreatePrinterDiscoverySession(): PrinterDiscoverySession? {
        return PrinterDiscoverySession(builder, applicationContext, this)
    }

    override fun onRequestCancelPrintJob(printJob: PrintJob) {
        printJob.cancel()
        Log.i(TAG, "#onRequestCancelPrintJob() printJobId: " + printJob.id)

    }

    override fun onPrintJobQueued(printJob: PrintJob) {
        Log.i(TAG, "override on Print Job Queued ")

        val permissionWrite = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val permissionRead = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        if (permissionWrite != PackageManager.PERMISSION_GRANTED
            || permissionRead != PackageManager.PERMISSION_GRANTED
        ) {
            Log.i(TAG, "Permission to write files denied")
            Toast.makeText(this, "Please login to Printerlogic app", Toast.LENGTH_LONG).show()
            printJob.cancel()
            return
        }


        Log.i(TAG, "Handle Queued Print Job")
        if (printJob.isQueued) {
            printJob.start()
        }
        val printerId = printJob.info.printerId
        val printerHashmap = PrinterHashmap()
        var finalUrl = "http" + "://" + printerHashmap.hashMap[printerId]!!
            .printerHost + ":" + printerHashmap.hashMap[printerId]!!.printerPort + "/ipp/print"

        finalUrl = finalUrl.replace("///", "//")

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
            val printRenderUtils = PrintRenderUtils()

            printRenderUtils.renderPageUsingDefaultPdfRenderer(
                file,
                finalUrl,
                this@PrinterLogicPrintService
            )

            printJob.complete()
        } catch (ioe: IOException) {
        }
    }


}

internal class PrinterDiscoverySession(
    printerInfo: PrinterInfo?,
    context: Context?,
    printService: PrinterLogicPrintService
) : PrinterDiscoverySession() {
    var printService: PrinterLogicPrintService
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
        val printerId = ArrayList<PrinterId>()
        val printerList = PrinterList()
        printerList.printerList.forEach(Consumer { p: PrinterModel ->
            printerId.add(printService.generatePrinterId(p.printerHost.toString()))
            val builder = PrinterInfo.Builder(
                printService.generatePrinterId(p.printerHost.toString()),
                p.serviceName, PrinterInfo.STATUS_IDLE
            ).build()
            val capabilities = printerId.let {
                PrinterCapabilitiesInfo.Builder(it.get(0))
                    .addMediaSize(PrintAttributes.MediaSize.ISO_A5, true)
                    .addResolution(PrintAttributes.Resolution("1234", "Default", 200, 200), true)
                    .setColorModes(
                        PrintAttributes.COLOR_MODE_MONOCHROME,
                        PrintAttributes.COLOR_MODE_MONOCHROME
                    )
                    .build()
            }
            printerInfo = capabilities.let {
                PrinterInfo.Builder(builder)
                    .setCapabilities(it)
                    .build()
            }
            printers.add(printerInfo)
            // filter out the printers

            val printerHashmap = PrinterHashmap()
            val hashMap: HashMap<PrinterId?, PrinterModel?> = HashMap()
            hashMap[printerId[0]] = p
            printerHashmap.hashMap = hashMap
        })

        //for loop

        // for loop ends
        addPrinters(printers);
    }

    override fun onStopPrinterDiscovery() {}
    override fun onValidatePrinters(printerIds: List<PrinterId>) {}
    override fun onStartPrinterStateTracking(printerId: PrinterId) {}
    override fun onStopPrinterStateTracking(printerId: PrinterId) {}
    override fun onDestroy() {}

    init {
        appContext = context
        this.printService = printService
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


