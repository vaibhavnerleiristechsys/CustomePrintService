package com.example.customeprintservice

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.*
import android.print.*
import android.print.pdf.PrintedPdfDocument
import android.printservice.PrintJob
import android.printservice.PrintService
import android.printservice.PrinterDiscoverySession
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.customeprintservice.jipp.PrintActivity
import com.example.customeprintservice.jipp.PrintUtils
import com.example.customeprintservice.jipp.PrinterList
import com.example.customeprintservice.jipp.PrinterModel
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import java.io.*
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList

class PrinterLogicPrintService : PrintService() {
    private val builder: PrinterInfo? = null
    private var mHandler: Handler? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    companion object {
        const val MSG_HANDLE_PRINT_JOB = 3
        private const val TAG = "PrintService"
    }

    override fun onConnected() {
        Log.i(TAG, "#onConnected()")
        mHandler = PrintHandler(mainLooper)
        firebaseAnalytics = Firebase.analytics
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
        Log.i(TAG, "override on Print Job Queued ")
        val message =
            mHandler!!.obtainMessage(MSG_HANDLE_PRINT_JOB, printJob)
        mHandler!!.sendMessageDelayed(message, 0)
//        val printManager = this
//            .getSystemService(Context.PRINT_SERVICE) as PrintManager
//        val jobName: String = this.getString(R.string.app_name).toString() +
//                " Document"
//        printManager.print(
//            jobName, MyPrintDocumentAdapter(this),
//            null
//        )
    }

    private fun handleHandleQueuedPrintJob(printJob: PrintJob) {
        Log.i(TAG, "Handle Queued Print Job")
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

//            printUtils.print(URI.create(finalUrl), file, applicationContext, "")
            val parameters = Bundle().apply {
                val isFileExists = file.exists()
                this.putString("isFileExists", isFileExists.toString())
            }
            firebaseAnalytics.setDefaultEventParameters(parameters)
            val bundle = Bundle()
            bundle.putString("fromPrintService", "fromPrintService")
            bundle.putString("finalUrl", finalUrl)
            bundle.putString("filePath", file.path)
            val intent = Intent(applicationContext, PrintActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtras(bundle)
            applicationContext.startActivity(intent)
            printJob.complete()

        } catch (ioe: IOException) {
        }
    }


    private inner class PrintHandler(looper: Looper?) : Handler(looper!!) {
        override fun handleMessage(message: Message) {
            Log.i(TAG, "---------------in Handle message----------")
            when (message.what) {
                Companion.MSG_HANDLE_PRINT_JOB -> {
                    val printJob = message.obj as PrintJob
                    handleHandleQueuedPrintJob(printJob)


//                  PrintUtils().getJobAttributes(URI.create(finalUrl),printJob,applicationContext)
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
        val printerId = ArrayList<PrinterId>()
        val printerList = PrinterList()
        printerList.printerList.forEach(Consumer { p: PrinterModel ->
            printerId.add(thermalPrintService.generatePrinterId(p.printerHost.toString()))
            val builder = PrinterInfo.Builder(
                thermalPrintService.generatePrinterId(p.printerHost.toString()),
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


class MyPrintDocumentAdapter(var context: Context) :
    PrintDocumentAdapter() {
    var myPdfDocument: PdfDocument? = null
    var totalpages = 4
    private var pageHeight = 0
    private var pageWidth = 0
    private fun pageInRange(pageRanges: Array<PageRange>, page: Int): Boolean {
        for (i in pageRanges.indices) {
            if (page >= pageRanges[i].start &&
                page <= pageRanges[i].end
            ) return true
        }
        return false
    }

    private fun drawPage(
        page: PdfDocument.Page,
        pagenumber: Int
    ) {
        var pagenumber = pagenumber
        val canvas = page.canvas
        pagenumber++ // Make sure page numbers start at 1
        val titleBaseLine = 72
        val leftMargin = 54
        val paint = Paint()
        paint.color = Color.BLACK
        paint.textSize = 40f
        canvas.drawText(
            "Test Print Document Page $pagenumber",
            leftMargin.toFloat(),
            titleBaseLine.toFloat(),
            paint
        )
        paint.textSize = 14f
        canvas.drawText(
            "This is some test content to verify that custom document printing works",
            leftMargin.toFloat(), titleBaseLine + 35.toFloat(), paint
        )
        if (pagenumber % 2 == 0) paint.color = Color.RED else paint.color = Color.GREEN
        val pageInfo = page.info
        canvas.drawCircle(
            pageInfo.pageWidth / 2.toFloat(),
            pageInfo.pageHeight / 2.toFloat(), 150f,
            paint
        )
    }

    override fun onLayout(
        oldAttributes: PrintAttributes,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        metadata: Bundle
    ) {
        myPdfDocument = PrintedPdfDocument(context, newAttributes)
        pageHeight = newAttributes.mediaSize!!.heightMils / 1000 * 72
        pageWidth = newAttributes.mediaSize!!.widthMils / 1000 * 72
        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }
        if (totalpages > 0) {
            val builder = PrintDocumentInfo.Builder("print_output.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(totalpages)
            val info = builder.build()
            callback.onLayoutFinished(info, true)
        } else {
            callback.onLayoutFailed("Page count is zero.")
        }
    }

    override fun onWrite(
        pageRanges: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        for (i in 0 until totalpages) {
            if (pageInRange(pageRanges, i)) {
                val newPage = PdfDocument.PageInfo.Builder(
                    pageWidth,
                    pageHeight, i
                ).create()
                val page = myPdfDocument!!.startPage(newPage)
                if (cancellationSignal.isCanceled) {
                    callback.onWriteCancelled()
                    myPdfDocument!!.close()
                    myPdfDocument = null
                    return
                }
                drawPage(page, i)
                myPdfDocument!!.finishPage(page)
            }
        }
        try {
            myPdfDocument!!.writeTo(
                FileOutputStream(
                    destination.fileDescriptor
                )
            )
        } catch (e: IOException) {
            callback.onWriteFailed(e.toString())
            return
        } finally {
            myPdfDocument!!.close()
            myPdfDocument = null
        }
        callback.onWriteFinished(pageRanges)
    }
}