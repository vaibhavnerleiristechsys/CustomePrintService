package com.printerlogic.printerlogic

//import org.slf4j.LoggerFactory
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.pdf.PdfRenderer
import android.os.Build
import android.os.ParcelFileDescriptor
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hp.jipp.model.Media
import com.printerlogic.printerlogic.jipp.PrintRenderUtils
import com.printerlogic.printerlogic.jipp.PrinterModel
import com.printerlogic.printerlogic.prefs.SignInCompanyPrefs
import com.printerlogic.printerlogic.print.BottomNavigationActivityForServerPrint
import com.printerlogic.printerlogic.print.PrintPreview
import com.printerlogic.printerlogic.print.PrintersFragment
import com.printerlogic.printerlogic.room.SelectedFile
import com.printerlogic.printerlogic.utils.DataDogLogger
import java.io.*
import java.net.InetAddress
import java.net.URI
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList


class PrinterLogicPrintService : PrintService() {
    private val builder: PrinterInfo? = null
  //  var logger = LoggerFactory.getLogger(PrinterLogicPrintService::class.java)


    companion object {
        const val MSG_HANDLE_PRINT_JOB = 3
        private const val TAG = "PrintService"
    }

    override fun onConnected() {
        Log.i(TAG, "#onConnected()")
        DataDogLogger.getLogger().i("Devnco_Android " + TAG + "#onConnected()")

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
           DataDogLogger.getLogger().i("Devnco_Android " + TAG + "Permission to record denied")
            Toast.makeText(this, "Please login to Printerlogic app", Toast.LENGTH_LONG).show()
        }

    }

    override fun onDisconnected() {
        super.onDisconnected()
        Log.i(TAG, "#onDisConnected()")
        DataDogLogger.getLogger().i("Devnco_Android " + TAG + "#onDisConnected()")
        stopSelf()
    }

    override fun onCreatePrinterDiscoverySession(): PrinterDiscoverySession? {
        return PrinterDiscoverySession(builder, applicationContext, this)
    }

    override fun onRequestCancelPrintJob(printJob: PrintJob) {
        printJob.cancel()
        Log.i(TAG, "#onRequestCancelPrintJob() printJobId: " + printJob.id)
        DataDogLogger.getLogger().i("Devnco_Android " + TAG + "#onRequestCancelPrintJob() printJobId: " + printJob.id)

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onPrintJobQueued(printJob: PrintJob) {
        Log.i(TAG, "override on Print Job Queued ")
        DataDogLogger.getLogger().i("Devnco_Android " + TAG + "override on Print Job Queued ")

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
        DataDogLogger.getLogger().i("Devnco_Android " + TAG + "Handle Queued Print Job")
        if (printJob.isQueued) {
            printJob.start()
        }
        val printerId = printJob.info.printerId
       val colorMode = printJob.info.attributes.colorMode
        Log.i("colorMode:", colorMode.toString())
        var isColor:Boolean=true;
        if(colorMode == 2){
            isColor =true;
        }else{
            isColor =false;
        }

        val pages =printJob.info.pages
        val paperSize:PrintAttributes.MediaSize? = printJob.info.attributes.mediaSize
        val orientation = printJob.info.attributes.mediaSize?.isPortrait
        val duplex =printJob.info.attributes.duplexMode
        val copies =printJob.info.copies
        var paperSizeValue:String=""

        //*******************************************
        var sharedPreferencesStoredPrinterListWithDetails = java.util.ArrayList<PrinterModel>()
        var deployedSecurePrinterListWithDetailsSharedPreflist = java.util.ArrayList<PrinterModel>()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val gson = Gson()
        val json = prefs.getString("prefServerSecurePrinterListWithDetails", null)
        val type = object :
            TypeToken<java.util.ArrayList<PrinterModel?>?>() {}.type

        // val deployedPrinterjson = prefs.getString("deployedsecurePrinterListWithDetails", null)
        val deployedPrinterjson = prefs.getString("deployedPrintersListForPrintPreivew", null)

        if (deployedPrinterjson != null) {
            deployedSecurePrinterListWithDetailsSharedPreflist = gson.fromJson(
                deployedPrinterjson,
                type
            )
        }


        if (json != null) {
            sharedPreferencesStoredPrinterListWithDetails = gson.fromJson<java.util.ArrayList<PrinterModel>>(
                json,
                type
            )
        }

        if (deployedSecurePrinterListWithDetailsSharedPreflist != null && deployedSecurePrinterListWithDetailsSharedPreflist.size > 0) {
            sharedPreferencesStoredPrinterListWithDetails.addAll(
                deployedSecurePrinterListWithDetailsSharedPreflist
            )
        }

        if (sharedPreferencesStoredPrinterListWithDetails != null) {
            for(printer in sharedPreferencesStoredPrinterListWithDetails){
                if(printer.mediaSupportList !=null){
                    for(media in printer.mediaSupportList) {

                        if (paperSize != null) {
                            Log.d("paperSize.id.toString()", paperSize.id.toString().toLowerCase())
                            Log.d(
                                "mediaSupportList",
                                media.toString().toLowerCase()
                            )
                            if (media.toString().toLowerCase()
                                    .contains(paperSize.id.toString().toLowerCase())
                            ) {
                                paperSizeValue = media.toString()
                            }
                        }
                    }
                }
            }

        }

        //*******************************************


       // paperSizeValue = PrintAttributes.MediaSize.ISO_A5.id.toString()
        if (paperSizeValue.equals("")) {
          //  Log.i("paperSize:", paperSize.id.toString())
            paperSizeValue = Media.isoA5_148x210mm.toString()
        }
        var orientationValue:String

        if(orientation == false){
            orientationValue="landscape"
        }else{
            orientationValue="portrait"
        }


        val printerHashmap = PrinterHashmap()
        var finalUrl = "http" + "://" + printerHashmap.hashMap[printerId]!!
            .printerHost + ":" + printerHashmap.hashMap[printerId]!!.printerPort + "/ipp/print"

        finalUrl = finalUrl.replace("///", "//")
        BottomNavigationActivityForServerPrint.selectedPrinter.printerHost=printerHashmap.hashMap[printerId]!!.printerHost
        BottomNavigationActivityForServerPrint.selectedPrinter.serviceName=printerHashmap.hashMap[printerId]!!.serviceName
        BottomNavigationActivityForServerPrint.selectedPrinter.id=printerHashmap.hashMap[printerId]!!.id
         var isPullPrinter:String =printerHashmap.hashMap[printerId]!!.isPullPrinter
        var  printerIds:String =printerHashmap.hashMap[printerId]!!.id
        val info = printJob.info
        val file = File(filesDir, info.label + ".pdf")
        var holdFile =File(filesDir, info.label)
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

        /*  var printPreview: PrintPreview =PrintPreview()
            dialogPromptPrinterForNavtiePrint(
                "alwaysPrompt",
                this@PrinterLogicPrintService
            )
*/
            if(isPullPrinter.equals("1") || isPullPrinter.equals("1.0") ){

                var printPreview: PrintPreview =PrintPreview()
                printPreview.sendHoldJobFromNativePrint(
                    file.path,
                    printerIds,
                    isPullPrinter,
                    this@PrinterLogicPrintService
                )

                printJob.complete()
              //  val intent = Intent(this@PrinterLogicPrintService, MainActivity::class.java)
               // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                //startActivity(intent)
              //  printPreview.selectePrinterDialoga(baseContext)
                val dialogIntent = Intent(this, MainActivity::class.java)
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(dialogIntent)
            }else {
                val fileDescriptor = ParcelFileDescriptor.open(
                    file,
                    ParcelFileDescriptor.MODE_READ_ONLY
                )
                val renderer = PdfRenderer(fileDescriptor)
                val pageCount = renderer.pageCount




                val ippUri = java.util.ArrayList<URI>()
                val hostAddress :String= printerHashmap.hashMap[printerId]!!.printerHost.toString()
                if (hostAddress != null) {
                    val printerHost: String = hostAddress
                    ippUri.add(URI.create("ipp:/$printerHost:631/ipp/print"))
                    ippUri.add(URI.create("ipp:/$printerHost:631/ipp/printer"))
                    ippUri.add(URI.create("ipp:/$printerHost:631/ipp/lp"))
                    ippUri.add(URI.create("ipp:/$printerHost/printer"))
                    ippUri.add(URI.create("ipp:/$printerHost/ipp"))
                    ippUri.add(URI.create("ipp:/$printerHost/ipp/print"))
                    ippUri.add(URI.create("http:/$printerHost:631/ipp"))
                    ippUri.add(URI.create("http:/$printerHost:631/ipp/print"))
                    ippUri.add(URI.create("http:/$printerHost:631/ipp/printer"))
                    ippUri.add(URI.create("http:/$printerHost:631/print"))
                    ippUri.add(URI.create("http:/$printerHost/ipp/print"))
                    ippUri.add(URI.create("http:/$printerHost"))
                    ippUri.add(URI.create("http:/$printerHost:631/printers/lp1"))
                    ippUri.add(URI.create("https:/$printerHost"))
                    ippUri.add(URI.create("https:/$printerHost:443/ipp/print"))
                    ippUri.add(URI.create("ipps:/$printerHost:443/ipp/print"))
                    ippUri.add(URI.create("http:/$printerHost:631/ipp/lp"))
                }



                if (duplex == 1) {
                    printRenderUtils.renderPageUsingDefaultPdfRendererForSelectedPages(
                        file,
                        finalUrl,
                        this@PrinterLogicPrintService,
                        1,
                        pageCount,
                        copies,
                        ippUri,
                        pageCount,
                        isColor,
                        orientationValue,
                        paperSizeValue
                    )
                } else {
               var paperSide ="two-sided-long-edge"
                    if(duplex == 2){
                        paperSide ="two-sided-long-edge"
                    }else{
                        paperSide ="two-sided-short-edge"
                    }
                    /*printRenderUtils.renderPageUsingDefaultPdfRendererForSelectedPagesForTwoSidedPrint(
                        file,
                        finalUrl,
                        this@PrinterLogicPrintService,
                        1,
                        pageCount,
                        copies,
                        ippUri,
                        pageCount,
                        isColor,
                        orientationValue,
                        paperSizeValue
                    )*/


                        printRenderUtils.printNoOfCOpiesJpgOrPngAndPdfFiles(
                            file,
                            finalUrl,
                            this@PrinterLogicPrintService,
                            copies,
                            ippUri,
                            isColor,
                            orientationValue,
                            paperSizeValue,
                            paperSide
                        )

                }


           /*     printRenderUtils.renderPageUsingDefaultPdfRenderer(
                    file,
                    finalUrl,
                    this@PrinterLogicPrintService,
                    printerHashmap.hashMap[printerId]!!.printerHost.toString(),
                    colorMode
                )

            */
            }

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
    private lateinit var appContext: Context
     var sharedPreferencesStoredPrinterListWithDetails = java.util.ArrayList<PrinterModel>()
     var deployedSecurePrinterListWithDetailsSharedPreflist = java.util.ArrayList<PrinterModel>()
   // var logger = LoggerFactory.getLogger(PrinterDiscoverySession::class.java)

    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.N)
    override fun onStartPrinterDiscovery(priorityList: List<PrinterId>) {
        Log.d("customprintservices", "onStartPrinterDiscovery")
        DataDogLogger.getLogger().i("Devnco_Android customprintservices" + "onStartPrinterDiscovery")


        PrintersFragment.discoveredPrinterListWithDetails.clear()
     /*   val printUtils = PrintUtils()
       printUtils.setContextAndInitializeJMDNS(appContext)
        try {
            Thread.sleep(3000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

      */
        val printers: ArrayList<PrinterInfo?> = ArrayList()
        val printerHashmap = PrinterHashmap()

        val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
        val gson = Gson()
        val json = prefs.getString("prefServerSecurePrinterListWithDetails", null)
        val type = object :
            TypeToken<java.util.ArrayList<PrinterModel?>?>() {}.type

       // val deployedPrinterjson = prefs.getString("deployedsecurePrinterListWithDetails", null)
        val deployedPrinterjson = prefs.getString("deployedPrintersListForPrintPreivew", null)

        if (deployedPrinterjson != null) {
            deployedSecurePrinterListWithDetailsSharedPreflist = gson.fromJson(
                deployedPrinterjson,
                type
            )
        }


        if (json != null) {
            @SuppressLint("WrongConstant")val sh: SharedPreferences = appContext.getSharedPreferences(
                "MySharedPref",
                Context.MODE_APPEND
            )
            val IsLdap = sh.getString("IsLdap", "")
            val LdapUsername= sh.getString("LdapUsername", "")
            val userName =sh.getString("userName", "")

            sharedPreferencesStoredPrinterListWithDetails.clear()
            var manualPrinterList = java.util.ArrayList<PrinterModel>()
            manualPrinterList = gson.fromJson<java.util.ArrayList<PrinterModel>>(
                json,
                type
            )


            manualPrinterList.forEach(Consumer { p: PrinterModel ->
               if(p.idpName !=null) {
                   if (p.printerAddedByUser.equals(userName) && p.idpName.equals(
                           SignInCompanyPrefs.getIdpName(
                               appContext
                           )
                       )
                   ) {
                       sharedPreferencesStoredPrinterListWithDetails.add(p);
                   } else if (p.printerAddedByUser.equals(LdapUsername) && p.idpName.equals(IsLdap)) {
                       sharedPreferencesStoredPrinterListWithDetails.add(p);
                   }
               }else{
                   sharedPreferencesStoredPrinterListWithDetails.add(p);
               }

            })


        }

        if (deployedSecurePrinterListWithDetailsSharedPreflist != null && deployedSecurePrinterListWithDetailsSharedPreflist.size > 0) {
            sharedPreferencesStoredPrinterListWithDetails.addAll(
                deployedSecurePrinterListWithDetailsSharedPreflist
            )
        }

        if (sharedPreferencesStoredPrinterListWithDetails != null) {
            var counter: Int =1
            sharedPreferencesStoredPrinterListWithDetails.forEach(Consumer { p: PrinterModel ->


                val printerId = ArrayList<PrinterId>()
                Log.d("service name", p.serviceName.toString())
                if (p.printerHost != null) {
                    var colorlist: ArrayList<String> = java.util.ArrayList<String>()
                    if (p.colorSupportList != null) {
                        for (item in p.colorSupportList) {
                            colorlist.add(item)
                        }
                    } else {
                        colorlist.add("color")
                    }

                    Log.d("host ", p.printerHost.toString())
                    DataDogLogger.getLogger().i("Devnco_Android host " + p.printerHost.toString())

                    if (p.isPullPrinter.equals("1.0") || p.isPullPrinter.equals("1")) {
                        // printerId.add(printService.generatePrinterId("/::"+counter))
                        p.printerHost = InetAddress.getByName("192.168.1." + counter)
                        counter++
                    }
                    printerId.add(printService.generatePrinterId(p.printerHost.toString()))
                    val builder = PrinterInfo.Builder(
                        printService.generatePrinterId(p.printerHost.toString()),
                        p.serviceName, PrinterInfo.STATUS_IDLE
                    ).build()
                    val capabilities = printerId.let {

                        val capBuilder: PrinterCapabilitiesInfo.Builder =
                            PrinterCapabilitiesInfo.Builder(
                                it.get(
                                    0
                                )
                            )

                        if (p.mediaSupportList != null) {
                            for (list in p.mediaSupportList) {
                                Log.d("media supported", list)
                                if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_A0.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A0, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_A1.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A1, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_A10.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.ISO_A10,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_A2.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A2, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_A3.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A3, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_A4.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A4, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_A5.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A5, true)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_A6.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A6, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_A7.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A7, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_A8.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A8, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_A9.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A9, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_B0.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B0, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_B1.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B1, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_B10.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.ISO_B10,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_B2.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B2, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_B3.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B3, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_B4.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B4, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_B5.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B5, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_B6.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B6, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_B7.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B7, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_B8.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B8, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_B9.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B9, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_C0.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_C0, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_C1.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_C1, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_C10.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.ISO_C10,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_C2.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_C2, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_C3.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_C3, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_C4.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_C4, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_C5.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_C5, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_C6.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_C6, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_C7.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_C7, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_C8.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_C8, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ISO_C9.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_C9, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JIS_B0.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.JIS_B0, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JIS_B1.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.JIS_B1, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JIS_B2.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.JIS_B2, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JIS_B3.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.JIS_B3, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JIS_B4.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.JIS_B4, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JIS_B5.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.JIS_B5, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JIS_B6.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.JIS_B6, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JIS_B7.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.JIS_B7, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JIS_B8.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.JIS_B8, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JIS_B9.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.JIS_B9, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JIS_B10.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.JIS_B10,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JIS_EXEC.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.JIS_EXEC,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JPN_CHOU2.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.JPN_CHOU2,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JPN_CHOU3.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.JPN_CHOU3,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JPN_CHOU4.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.JPN_CHOU4,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JPN_HAGAKI.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.JPN_HAGAKI,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JPN_KAHU.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.JPN_KAHU,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JPN_KAKU2.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.JPN_KAKU2,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JPN_OUFUKU.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.JPN_OUFUKU,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.JPN_YOU4.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.JPN_YOU4,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.NA_FOOLSCAP.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.NA_FOOLSCAP,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.NA_GOVT_LETTER.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.NA_GOVT_LETTER,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.NA_INDEX_3X5.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.NA_INDEX_3X5,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.NA_INDEX_4X6.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.NA_INDEX_4X6,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.NA_INDEX_5X8.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.NA_INDEX_5X8,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.NA_JUNIOR_LEGAL.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.NA_JUNIOR_LEGAL,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.NA_LEDGER.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.NA_LEDGER,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.NA_LEGAL.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.NA_LEGAL,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.NA_LETTER.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.NA_LETTER,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.NA_MONARCH.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.NA_MONARCH,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.NA_QUARTO.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.NA_QUARTO,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.NA_TABLOID.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.NA_TABLOID,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.OM_DAI_PA_KAI.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.OM_DAI_PA_KAI,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.OM_JUURO_KU_KAI.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.OM_JUURO_KU_KAI,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.OM_PA_KAI.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.OM_PA_KAI,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.PRC_1.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.PRC_1, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.PRC_10.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.PRC_10, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.PRC_16K.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.PRC_16K,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.PRC_2.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.PRC_2, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.PRC_3.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.PRC_3, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.PRC_4.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.PRC_4, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.PRC_5.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.PRC_5, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.PRC_6.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.PRC_6, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.PRC_7.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.PRC_7, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.PRC_8.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.PRC_8, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.PRC_9.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.PRC_9, false)
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ROC_16K.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(
                                        PrintAttributes.MediaSize.ROC_16K,
                                        false
                                    )
                                } else if (list.toLowerCase()
                                        .contains(PrintAttributes.MediaSize.ROC_8K.id.toLowerCase())
                                ) {
                                    capBuilder.addMediaSize(PrintAttributes.MediaSize.ROC_8K, false)
                                }


                            }
                        } else {
                            if (p.isPullPrinter.equals("1") || p.isPullPrinter.equals("1.0")) {
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A0, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A1, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A10, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A2, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A3, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A5, true)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A6, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A4, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A7, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A8, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A9, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B0, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B1, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B10, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B2, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B4, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_B5, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_C1, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_C4, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_C9, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.JIS_B0, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.JIS_B6, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.JIS_EXEC, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.JPN_HAGAKI, false)
                                capBuilder.addMediaSize(
                                    PrintAttributes.MediaSize.NA_JUNIOR_LEGAL,
                                    false
                                )
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.NA_LEDGER, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.PRC_1, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.PRC_8, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.PRC_3, false)
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ROC_16K, false)
                            } else {
                                capBuilder.addMediaSize(PrintAttributes.MediaSize.ISO_A5, true)
                            }
                        }

                        capBuilder
                            .addResolution(
                                PrintAttributes.Resolution("1234", "Default", 200, 200),
                                true
                            )
                        for (list in colorlist) {
                            if (list.toLowerCase().contains("monochrome")) {
                                capBuilder.setColorModes(
                                    PrintAttributes.COLOR_MODE_COLOR + PrintAttributes.COLOR_MODE_MONOCHROME,
                                    PrintAttributes.COLOR_MODE_COLOR
                                )
                            } else {
                                if (p.isPullPrinter.equals("1")) {
                                    capBuilder.setColorModes(
                                        PrintAttributes.COLOR_MODE_COLOR + PrintAttributes.COLOR_MODE_MONOCHROME,
                                        PrintAttributes.COLOR_MODE_COLOR
                                    )
                                } else {
                                    capBuilder.setColorModes(
                                        PrintAttributes.COLOR_MODE_COLOR,
                                        PrintAttributes.COLOR_MODE_COLOR
                                    )
                                }
                            }
                        }

                        if (p.sidesSupportList != null) {
                            for (list in p.sidesSupportList) {
                                if (list.toLowerCase().contains("two-sided")) {
                                    capBuilder
                                        .setDuplexModes(
                                            PrintAttributes.DUPLEX_MODE_NONE + PrintAttributes.DUPLEX_MODE_LONG_EDGE + PrintAttributes.DUPLEX_MODE_SHORT_EDGE,
                                            PrintAttributes.DUPLEX_MODE_NONE
                                        )
                                } else {
                                    capBuilder
                                        .setDuplexModes(
                                            PrintAttributes.DUPLEX_MODE_NONE,
                                            PrintAttributes.DUPLEX_MODE_NONE
                                        )
                                }
                            }
                        } else {
                            capBuilder
                                .setDuplexModes(
                                    PrintAttributes.DUPLEX_MODE_NONE,
                                    PrintAttributes.DUPLEX_MODE_NONE
                                )
                        }
                        capBuilder.build()
                    }
                    printerInfo = capabilities.let {
                        PrinterInfo.Builder(builder)
                            .setCapabilities(it)
                            .build()
                    }

                    /*    if(printerInfo.id.localId. .equals(/::1)){

                    }

                 */
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
        if (context != null) {
            appContext = context
        }
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


