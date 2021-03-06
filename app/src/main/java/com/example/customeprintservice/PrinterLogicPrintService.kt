package com.example.customeprintservice

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.preference.PreferenceManager
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
import com.example.customeprintservice.jipp.PrinterModel
import com.example.customeprintservice.print.BottomNavigationActivityForServerPrint
import com.example.customeprintservice.print.PrintersFragment
import com.example.customeprintservice.room.SelectedFile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.slf4j.LoggerFactory
import java.io.*
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList


class PrinterLogicPrintService : PrintService() {
    private val builder: PrinterInfo? = null
    var logger = LoggerFactory.getLogger(PrinterLogicPrintService::class.java)


    companion object {
        const val MSG_HANDLE_PRINT_JOB = 3
        private const val TAG = "PrintService"
    }

    override fun onConnected() {
        Log.i(TAG, "#onConnected()")
        logger.info(TAG, "#onConnected()")

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
            logger.info(TAG, "Permission to record denied")
            Toast.makeText(this, "Please login to Printerlogic app", Toast.LENGTH_LONG).show()
        }

    }

    override fun onDisconnected() {
        super.onDisconnected()
        Log.i(TAG, "#onDisConnected()")
        logger.info(TAG, "#onDisConnected()")
        stopSelf()
    }

    override fun onCreatePrinterDiscoverySession(): PrinterDiscoverySession? {
        return PrinterDiscoverySession(builder, applicationContext, this)
    }

    override fun onRequestCancelPrintJob(printJob: PrintJob) {
        printJob.cancel()
        Log.i(TAG, "#onRequestCancelPrintJob() printJobId: " + printJob.id)
        logger.info(TAG, "#onRequestCancelPrintJob() printJobId: " + printJob.id)

    }

    override fun onPrintJobQueued(printJob: PrintJob) {
        Log.i(TAG, "override on Print Job Queued ")
        logger.info(TAG, "override on Print Job Queued ")

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
        logger.info(TAG, "Handle Queued Print Job")
        if (printJob.isQueued) {
            printJob.start()
        }
        val printerId = printJob.info.printerId
        val printerHashmap = PrinterHashmap()
        var finalUrl = "http" + "://" + printerHashmap.hashMap[printerId]!!
            .printerHost + ":" + printerHashmap.hashMap[printerId]!!.printerPort + "/ipp/print"

        finalUrl = finalUrl.replace("///", "//")
        BottomNavigationActivityForServerPrint.selectedPrinter.printerHost=printerHashmap.hashMap[printerId]!!.printerHost
        BottomNavigationActivityForServerPrint.selectedPrinter.serviceName=printerHashmap.hashMap[printerId]!!.serviceName
        BottomNavigationActivityForServerPrint.selectedPrinter.id=printerHashmap.hashMap[printerId]!!.id
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
            BottomNavigationActivityForServerPrint.selectedServerFile.clear()
            val selectedFile :SelectedFile= SelectedFile()
            selectedFile.fileName=file.name
            selectedFile.filePath=file.path
            BottomNavigationActivityForServerPrint.selectedServerFile.add(selectedFile)

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
     var sharedPreferencesStoredPrinterListWithDetails = java.util.ArrayList<PrinterModel>()
    var logger = LoggerFactory.getLogger(PrinterDiscoverySession::class.java)

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onStartPrinterDiscovery(priorityList: List<PrinterId>) {
        Log.d("customprintservices", "onStartPrinterDiscovery")
        logger.info("customprintservices", "onStartPrinterDiscovery")

      val printUtils = PrintUtils()
        PrintersFragment.discoveredPrinterListWithDetails.clear()
        printUtils.setContextAndInitializeJMDNS(appContext)
        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        val printers: ArrayList<PrinterInfo?> = ArrayList()
        val printerHashmap = PrinterHashmap()

        val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
        val gson = Gson()
        val json = prefs.getString("prefServerSecurePrinterListWithDetails", null)
        val type = object :
            TypeToken<java.util.ArrayList<PrinterModel?>?>() {}.type
        if (json != null) {
            sharedPreferencesStoredPrinterListWithDetails = gson.fromJson<java.util.ArrayList<PrinterModel>>(json, type)
        }

        if (sharedPreferencesStoredPrinterListWithDetails != null) {
            sharedPreferencesStoredPrinterListWithDetails.forEach(Consumer { p: PrinterModel ->
                val printerId = ArrayList<PrinterId>()
                Log.d("service name",p.serviceName.toString())
                if(p.printerHost!=null) {
                    Log.d("host ", p.printerHost.toString())
                    logger.info("host ", p.printerHost.toString())
                    printerId.add(printService.generatePrinterId(p.printerHost.toString()))
                    val builder = PrinterInfo.Builder(
                        printService.generatePrinterId(p.printerHost.toString()),
                        p.serviceName, PrinterInfo.STATUS_IDLE
                    ).build()
                    val capabilities = printerId.let {
                        PrinterCapabilitiesInfo.Builder(it.get(0))
                            .addMediaSize(PrintAttributes.MediaSize.ISO_A5, true)
                            .addResolution(
                                PrintAttributes.Resolution("1234", "Default", 200, 200),
                                true
                            )
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

                    printerHashmap.hashMap.put(printerId[0], p)
                }
            })

            if(printers!=null) {
                addPrinters(printers);
            }
        }
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


