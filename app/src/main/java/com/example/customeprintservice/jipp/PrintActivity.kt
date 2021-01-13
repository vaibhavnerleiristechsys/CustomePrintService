package com.example.customeprintservice.jipp


//import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
//import org.apache.pdfbox.rendering.ImageType
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aspose.words.Document
import com.example.customeprintservice.R
import com.example.customeprintservice.adapter.SelectedFileListAdapter
import com.example.customeprintservice.model.DecodedJWTResponse
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.prefs.SignInCompanyPrefs
import com.example.customeprintservice.printjobstatus.PrinterListService
import com.example.customeprintservice.room.SelectedFile
import com.example.customeprintservice.signin.SignInCompany
import com.example.customeprintservice.utils.JwtDecode
import com.example.customeprintservice.utils.ProgressDialog
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_print.*
import org.jetbrains.anko.toast
import java.io.File
import java.net.URI


class PrintActivity : AppCompatActivity() {

    val printUtils = PrintUtils()
    var bundle: Bundle = Bundle()
    var uri: URI? = null

    var list = ArrayList<SelectedFile>()
    var selectedFileString = ""
    private val storageDir =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            .toString() + File.separator
    private val outputPDF = storageDir + "Converted_PDF.pdf"
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print)
        firebaseAnalytics = Firebase.analytics

        val printerList = PrinterList()
        val printerNameList = ArrayList<String>()
        printerList.printerList.forEach { s ->
            run {
                printerNameList.add(s.serviceName)
            }
        }
        val printerCommavalues = printerNameList.joinToString()
        Log.i("printer", "printerNamelist ==>${printerCommavalues}")

        firebaseAnalytics.setDefaultEventParameters(debugString(printerCommavalues, "printerName"))

        val actionBar = supportActionBar
        actionBar?.title = "Print"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        if (LoginPrefs.getOCTAToken(this@PrintActivity) == null) {
            val intent = Intent(this@PrintActivity, SignInCompany::class.java)
            startActivity(intent)
        }
        bundle = intent.extras!!

        if (bundle.getString("fromPrintService") == "fromPrintService") {
            val url = bundle.getString("finalUrl")
            val filePath = bundle.getString("filePath")
            val file = File(filePath)
            Log.i("printer", "url in bundle---->$url")
            edtPrinterActivityEditUrl.setText(url)
        } else if ((bundle.getSerializable("selectedFileList") as ArrayList<SelectedFile>?)!! != null) {

            val selectedFile: String? = bundle.getString("selectedFile")
            val ipAddress: String? = bundle.getString("ipAddress")
            val printerName: String? = bundle.getString("printerName")
            val formatSupported: String? = bundle.getString("formatSupported")

            list = (bundle.getSerializable("selectedFileList") as ArrayList<SelectedFile>?)!!

            Log.i(
                "printer",
                "selectedFileList----->" + bundle.getStringArrayList("selectedFileList")
            )

            txtDignosticInfo.text = bundle.getString("printerAttribute")

            txtPrinterActivityPrinterName.text = "Printer Name - ${printerName.toString()}"
            txtPrinterActivityFormatSupported.text =
                "format Supported -${formatSupported.toString()}"

            uri =
                URI.create(
                    "http://" + (bundle.getString("ipAddress")?.replace("/", "") ?: "") + ":${bundle.getString("printerPort")}" + "/" + "ipp/print"
                )
            Log.i("printer", "uri1---->$uri")

            edtPrinterActivityEditUrl.setText(uri.toString())
        }


        btnPrint.setOnClickListener {
            if (bundle.getString("fromPrintService") == "fromPrintService") {
                val url = bundle.getString("finalUrl")
                val filePath = bundle.getString("filePath")
                val file = File(filePath)
                printUtils.print(URI.create(url), file, this@PrintActivity, "")
            } else {
                dialogSelectedFileList()
            }
        }

        val filter = IntentFilter()
        filter.addAction("com.example.PRINT_RESPONSE")
        val receiver = broadcastReceiver
        registerReceiver(receiver, filter)


        btnSessionId.setOnClickListener {
            /**
             * get Session id
             */
            ProgressDialog.showLoadingDialog(this@PrintActivity, "Getting Session and Node id")
            PrinterListService().getPrinterNodeSession(
                this@PrintActivity,
                SignInCompanyPrefs.getIdpName(this@PrintActivity).toString(),
                true,
                decodeJWT(),
                "saml2",
                LoginPrefs.getOCTAToken(this@PrintActivity).toString(), true

            )
        }

        btnGetPrinterDetails.setOnClickListener {
            ProgressDialog.showLoadingDialog(this@PrintActivity, "Getting Printer Details")
            PrinterListService().getPrinterDetails(
                this@PrintActivity,
                LoginPrefs.getOCTAToken(this@PrintActivity).toString(),
                decodeJWT(),
                SignInCompanyPrefs.getIdpType(this@PrintActivity).toString(),
                SignInCompanyPrefs.getIdpName(this@PrintActivity).toString(),
                ""
            )
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastReceiver)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }


    var printResponseStatus: String = ""
    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            try {
                var printResponse: String = ""
                if (intent.getStringExtra("printResponse") != null) {
                    printResponse = intent.getStringExtra("printResponse").toString()
                    txtPrinterResponse.text = "Print Response - $printResponse"

                    firebaseAnalytics.setDefaultEventParameters(
                        debugString(
                            printResponse,
                            "printResponse"
                        )
                    )
                }

                var printerSupportedFormats: String = ""
                if (intent.getStringExtra("printerSupportedFormats") != null) {
                    printerSupportedFormats =
                        intent.getStringExtra("printerSupportedFormats").toString()
                    txtPrinterActivityFormatSupported.text =
                        "Printer Supported Format - $printerSupportedFormats"
                    firebaseAnalytics.setDefaultEventParameters(
                        debugString(
                            printerSupportedFormats,
                            "printerSupportedFormats"
                        )
                    )
                }

                var getPrinterAttributes: String = ""
                if (intent.getStringExtra("getPrinterAttributes") != null) {
                    getPrinterAttributes = intent.getStringExtra("getPrinterAttributes").toString()
                    txtDignosticInfo.text = "Get Attributes - $getPrinterAttributes"

//                    firebaseAnalytics.setDefaultEventParameters(debugString(getPrinterAttributes,"attributes"))

                }

                var exception: String = ""
                if (intent.getStringExtra("exception") != null) {
                    exception = intent.getStringExtra("exception").toString()
                    txtDignosticInfo.text = "Exception Occured - $exception"
                }

                var fileNotSupported: String = ""
                if (intent.getStringExtra("fileNotSupported") != null) {
                    fileNotSupported = intent.getStringExtra("fileNotSupported").toString()
                    Toast.makeText(this@PrintActivity, fileNotSupported, Toast.LENGTH_LONG).show()

                }
                if (intent.getStringExtra("printResponseStatus") != null) {
                    printResponseStatus = intent.getStringExtra("printResponseStatus").toString()
                    Log.i("printer", "printResponseStatus=>$printResponseStatus")
                }
            } catch (e: Exception) {
                txtDignosticInfo.text = e.toString()
            }
        }
    }

    fun debugString(str: String, key: String): Bundle {
        val strLength = str.length
        val strLengthMod = strLength / 100
        val bundle = Bundle()

        //55
        for (i in 0 until strLengthMod) {
            //655
            val subStr = str.substring(i * 100, i * 100 + 99)
            bundle.putString(key + i, subStr)
        }
        val remainingChars = strLength - strLengthMod * 100
        val remainingSubstring =
            str.substring(strLengthMod * 100, strLengthMod * 100 + remainingChars - 1)
        bundle.putString(key + "remaining", remainingSubstring)
        //bundle.putString("key","value");
        return bundle
    }

    @SuppressLint("WrongConstant")
    private fun dialogSelectedFileList() {
        val dialog = Dialog(this@PrintActivity)
        dialog.setContentView(R.layout.dialog_selected_file_list)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        dialog.window?.setLayout((6 * width) / 7, WindowManager.LayoutParams.WRAP_CONTENT)

        val btnProceedPrint = dialog.findViewById<Button>(R.id.btnProceedPrint)
        val btnCancelDialog = dialog.findViewById<Button>(R.id.btnDialogSelectedFileCancel)
        btnCancelDialog.setOnClickListener { dialog.cancel() }
        val recyclerViewSelectedFileLst =
            dialog.findViewById<RecyclerView>(R.id.recyclerViewSelectedFileList)
        recyclerViewSelectedFileLst.layoutManager =
            LinearLayoutManager(
                this@PrintActivity,
                LinearLayout.VERTICAL,
                false
            )

        val adapter = SelectedFileListAdapter(
            this@PrintActivity,
            list
        )
        adapter.itemClick().doOnNext {
            selectedFileString = it
            Log.i("printer", "selected file in item click -->$selectedFileString")
        }.subscribe()

        recyclerViewSelectedFileLst.adapter = adapter
        var pageImage: Bitmap
        btnProceedPrint.setOnClickListener {
            ProgressDialog.showLoadingDialog(dialog.context, "Proceeding...")
            var file = File(selectedFileString)
            if (file.extension.toLowerCase() == "docx" || file.extension.toLowerCase() == "doc") {
                Log.i("printer", "selected doc file")
//                file = File("/storage/emulated/0/Movies/${file.nameWithoutExtension}.pdf")
//                DocToPDF().ConvertToPDF(selectedFileString,"/storage/emulated/0/Movies/${file.nameWithoutExtension}.pdf")

                val inputStream =
                    contentResolver.openInputStream(Uri.fromFile(File(selectedFileString)))
                val document = Document(inputStream)
                document.watermark.remove()
                document.save(outputPDF)

                val parameters = Bundle().apply {
                    this.putString("outputpdf", outputPDF)
                }
                firebaseAnalytics.setDefaultEventParameters(parameters)

                Log.i("printer", "path saved =>${outputPDF}")
                file = File(outputPDF)
            } else if (file.extension.toLowerCase() == "pdf") {

                val printRenderUtils = PrintRenderUtils()

                //printRenderUtils.renderPage(file,edtPrinterActivityEditUrl.text.toString(),this@PrintActivity)
                printRenderUtils.renderPageUsingDefaultPdfRenderer(
                    file,
                    edtPrinterActivityEditUrl.text.toString(),
                    this@PrintActivity
                )


                /*try {
                    val finalUri = URI.create(edtPrinterActivityEditUrl.text.toString())

                    val pdfInputStream: InputStream =
                        BufferedInputStream(FileInputStream(file))

                    val document: PDDocument = PDDocument.load(pdfInputStream)
                    val pages: PDPageTree = document.pages
                    val renderer = PDFRenderer(document)

                    val totalNoOfPages: Int = pages.count
                    var pagePrintCounter: Int = 0

                    val thread = Thread {
                        while (pagePrintCounter < totalNoOfPages) {
                            pageImage =
                                renderer.renderImage(pagePrintCounter, 1F, Bitmap.Config.RGB_565)
                            val path = "/storage/self/primary/sample-$pagePrintCounter.jpg"
                            val renderFile = File(path)
                            val fileOut = FileOutputStream(renderFile)
                            pageImage.compress(Bitmap.CompressFormat.JPEG, 100, fileOut)
                            fileOut.close()
                            val map = printUtils.print(finalUri, renderFile, this@PrintActivity, "")

                            if (map.get("status") == "server-error-busy") {
                                Thread.sleep(5000)
                            } else {
                                pagePrintCounter++
                            }
                        }


//

                    }.start()
                } catch (e: IOException) {
                    Log.e("printer", "Exception=>", e)
                }*/

            } else {
                file = File(selectedFileString)
                Log.i("printer", "in else")
                val finalUri = URI.create(edtPrinterActivityEditUrl.text.toString())
                Thread {
                   val map =  printUtils.print(finalUri, file, this@PrintActivity, "")
                }.start()
            }

            val fileName: String = file.name
            var format: String? = null

            if (fileName.contains(".")) {
                format =
                    PrintUtils.extensionTypes[fileName.substring(fileName.lastIndexOf(".") + 1)]?.toLowerCase()
                        ?.trim()
                Log.i("printer", "format--->$format")
            }

//            val finalUri = URI.create(edtPrinterActivityEditUrl.text.toString())
//            Log.i("printer", "finalUrl --- >$finalUri")
//            printUtils.print(finalUri, file, this@PrintActivity, format)

//            printUtils.getJobs(finalUri, this@PrintActivity)
//
//            printUtils.getJobAttributes(finalUri, 1, this@PrintActivity)
//
//            printUtils.cancelJob(finalUri, 1, this@PrintActivity)

            dialog.cancel()
            ProgressDialog.cancelLoading()
        }
    }


    private fun decodeJWT(): String {
        var userName: String? = null
        try {
            val mapper = jacksonObjectMapper()
            val decoded: DecodedJWTResponse = mapper.readValue<DecodedJWTResponse>(
                LoginPrefs.getOCTAToken(this@PrintActivity)?.let { JwtDecode.decoded(it) }!!
            )
            userName = decoded.user.toString()
        } catch (ex: Exception) {
          //  toast("Failed to Decode Jwt Token")
        }
        return userName.toString()
    }

//    private val DPI = 300
//    private val IMAGE_TYPE = ImageType.RGB
//    private val RED_COEFFICIENT = 0.2126
//    private val GREEN_COEFFICIENT = 0.7512
//    private val BLUE_COEFFICIENT = 0.0722
//
//    var colorSpace = convertImageTypeToColorSpace(IMAGE_TYPE)
//
//    private fun convertPDFtoPCL(uriFile: String) {
//
//        val file = File(uriFile)
//        val filePclName = "${file.nameWithoutExtension}.pclm"
//
//        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
//
//        val outputFormat = OutputFormat.toOutputFormat(getExtension(filePclName))
//
//        val pdfInputStream: InputStream = BufferedInputStream(FileInputStream(File(uriFile)))
//
//        val outputStream: OutputStream = BufferedOutputStream(
//            FileOutputStream(File(path, "/$filePclName"))
//        )
//
//        Log.i("printer", "file path-- >$path")
//        Log.i("printer", "outputStream--->$outputStream")
//
//        try {
//            CustomPDDocument.load(pdfInputStream).use { document ->
//                val pdfRenderer = CustomPDFRenderer(document)
//                val pages = document.pages
//                val renderablePages: MutableList<RenderablePage> = ArrayList()
//
//                Log.i("printer", "in covert method")
//
//                for (pageIndex in 0 until pages.count) {
//                    var bitmapFactory: BitmapFactory
////                    val bitmap = Bitmap.createBitmap(
////                        resources.displayMetrics.densityDpi * pages.getWidth() / 72,
////                        resources.displayMetrics.densityDpi * mCurrentPage.getHeight() / 72,
////                        Bitmap.Config.ARGB_8888
////                    )
//                    val renderablePage: RenderablePage = object : RenderablePage(1001, 1001) {
//                        override fun render(
//                            yOffset: Int,
//                            swathHeight: Int,
//                            colorSpace: ColorSpace,
//                            byteArray: ByteArray
//                        ) {
//                            val red = 0
//                            val green = 13
//                            val blue = 0
//                            var rgb = 0
//                            var byteIndex = 0
//                            for (y in yOffset until yOffset + swathHeight) {
//                                for (x in 0..566) {
//
////                                    rgb = image.getRGB(x, y)
////                                    red = (rgb >> 16) & 0xFF
////                                    green = (rgb > > 8) & 0xFF
////                                    blue = rgb & 0xFF
//                                    if (colorSpace == ColorSpace.Grayscale) {
//                                        byteArray[byteIndex++] =
//                                            (RED_COEFFICIENT * red + GREEN_COEFFICIENT * green + BLUE_COEFFICIENT * blue).toByte()
//                                    } else {
//                                        byteArray[byteIndex++] = red.toByte()
//                                        byteArray[byteIndex++] = green.toByte()
//                                        byteArray[byteIndex++] = blue.toByte()
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    renderablePages.add(renderablePage)
//                }
//                val renderableDocument: RenderableDocument = object : RenderableDocument() {
//                    override fun iterator(): Iterator<RenderablePage> {
//                        return renderablePages.iterator()
//                    }
//
//                    override val dpi: Int
//                        get() = DPI
//                }
//                when (outputFormat) {
//                    OutputFormat.PCLM -> saveRenderableDocumentAsPCLm(
//                        renderableDocument,
//                        colorSpace!!,
//                        outputStream
//                    )
//                    OutputFormat.PWG_RASTER -> saveRenderableDocumentAsPWG(
//                        renderableDocument,
//                        colorSpace!!,
//                        outputStream
//                    )
//                }
//            }
//        } catch (e: InvalidPasswordException) {
//            e.printStackTrace()
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    private fun convertImageTypeToColorSpace(imageType: ImageType): ColorSpace? {
//        return when (imageType) {
//            ImageType.BINARY, ImageType.GRAY -> ColorSpace.Grayscale
//            else -> ColorSpace.Rgb
//        }
//    }
//
//    @Throws(IOException::class)
//    private fun saveRenderableDocumentAsPCLm(
//        renderableDocument: RenderableDocument,
//        colorSpace: ColorSpace, outputStream: OutputStream
//    ) {
//        val outputSettings =
//            OutputSettings(colorSpace, Sides.oneSided, MediaSource.auto, null, false)
//        val caps = PclmSettings(outputSettings, 32)
//        val writer = PclmWriter(outputStream, caps)
//        writer.write(renderableDocument)
//        writer.close()
//    }
//
//    @Throws(IOException::class)
//    private fun saveRenderableDocumentAsPWG(
//        renderableDocument: RenderableDocument,
//        colorSpace: ColorSpace, outputStream: OutputStream
//    ) {
//        val outputSettings =
//            OutputSettings(colorSpace, Sides.oneSided, MediaSource.auto, null, false)
//        val caps = PwgSettings(outputSettings)
//        val writer = PwgWriter(outputStream, caps)
//        writer.write(renderableDocument)
//        writer.close()
//    }
//
//    private fun getExtension(name: String): String {
//        val index = name.lastIndexOf(".")
//        require(!(index == -1 || index <= name.lastIndexOf("/"))) { "$name has no extension" }
//        return name.substring(index + 1)
//    }
//
//    enum class OutputFormat(val name1: String) {
//        PWG_RASTER("pwg"),
//        PCLM("PCLm");
//
//        companion object {
//            fun toOutputFormat(formatName: String): OutputFormat {
//                for (format in values()) {
//                    if (format.name1.equals(formatName, ignoreCase = true)) {
//                        return format
//                    }
//                }
//                throw IllegalArgumentException("Output format $formatName is invalid")
//            }
//        }
//    }




   fun locaPrint(selectedFileString:String,printerString:String,context: Context){
       Toast.makeText(context, printerString.toString(), Toast.LENGTH_LONG)
           .show()
       var file = File(selectedFileString)
       if (file.extension.toLowerCase() == "docx" || file.extension.toLowerCase() == "doc") {
           Log.i("printer", "selected doc file")


           val inputStream =
               contentResolver.openInputStream(Uri.fromFile(File(selectedFileString)))
           val document = Document(inputStream)
           document.watermark.remove()
           document.save(outputPDF)

           val parameters = Bundle().apply {
               this.putString("outputpdf", outputPDF)
           }
           firebaseAnalytics.setDefaultEventParameters(parameters)

           Log.i("printer", "path saved =>${outputPDF}")
           file = File(outputPDF)
       } else if (file.extension.toLowerCase() == "pdf") {

           val printRenderUtils = PrintRenderUtils()

           printRenderUtils.renderPageUsingDefaultPdfRenderer(
               file,
               printerString,
               context
           )



       } else {
           file = File(selectedFileString)
           Log.i("printer", "in else")
           val finalUri = URI.create(printerString)
           Thread {
               val map =  printUtils.print(finalUri, file, context, "")
           }.start()
       }

       val fileName: String = file.name
       var format: String? = null

       if (fileName.contains(".")) {
           format =
               PrintUtils.extensionTypes[fileName.substring(fileName.lastIndexOf(".") + 1)]?.toLowerCase()
                   ?.trim()
           Log.i("printer", "format--->$format")
       }
       ProgressDialog.cancelLoading()
   }

}