package com.example.customeprintservice.jipp


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.R
import com.example.customeprintservice.adapter.SelectedFileListAdapter
import com.hp.jipp.model.MediaSource
import com.hp.jipp.model.Sides
import com.hp.jipp.pdl.ColorSpace
import com.hp.jipp.pdl.OutputSettings
import com.hp.jipp.pdl.RenderableDocument
import com.hp.jipp.pdl.RenderablePage
import com.hp.jipp.pdl.pclm.PclmSettings
import com.hp.jipp.pdl.pclm.PclmWriter
import com.hp.jipp.pdl.pwg.PwgSettings
import com.hp.jipp.pdl.pwg.PwgWriter
import kotlinx.android.synthetic.main.activity_print.*
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException
import org.apache.pdfbox.rendering.ImageType
import java.io.*
import java.net.URI


class PrintActivity : AppCompatActivity() {

    val printUtils = PrintUtils()
    var bundle: Bundle = Bundle()
    var uri: URI? = null

    var list = ArrayList<String>()
    var selectedFileString = ""

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

            list = bundle.getStringArrayList("selectedFileList")!!

            Log.i(
                "printer",
                "selectedFileList----->" + bundle.getStringArrayList("selectedFileList")
            )

            txtDignosticInfo.text = bundle.getString("printerAttribute")
            txtPrinterActivitySelectedDocument.text =
                "Selected Document - ${selectedFile.toString()}"
            txtPrinterActivityPrinterName.text = "Printer Name - ${printerName.toString()}"
            txtPrinterActivityFormatSupported.text =
                "format Supported -${formatSupported.toString()}"

//            val file: File = File(selectedFile!!)
//            if (file.extension.toLowerCase() == "pdf") {
//                convertPDFtoPCL(selectedFile)
//            }
        }

        uri =
            URI.create("http://" + (bundle.getString("ipAddress")?.replace("/","") ?: "") + ":${bundle.getString("printerPort")}" + "/" + "ipp/print")
        Log.i("printer", "uri1---->$uri")

        edtPrinterActivityEditUrl.setText(uri.toString())
        btnPrint.setOnClickListener {
            dialogSelectedFileList()
//            val file = File(bundle.getString("selectedFile")!!)
//            val inputFile = File(file.absolutePath)
//            val fileName: String = inputFile.name
//            var format: String? = null
//            if (fileName.contains(".")) {
//                format =
//                    PrintUtils.extensionTypes[fileName.substring(fileName.lastIndexOf(".") + 1)]?.toLowerCase()
//                        ?.trim()
//                Log.i("printer", "format--->$format")
//            }
//
//            val finalUri = URI.create(edtPrinterActivityEditUrl.text.toString())
//            Log.i("printer", "finalUrl --- >$finalUri")
//            printUtils.print(finalUri, file, this@PrintActivity, format)

            if (fileName.contains(".")) {
                format = PrintUtils.extensionTypes[fileName.substring(fileName.lastIndexOf(".") + 1)
                    .toLowerCase()]
                Log.i("printer", "format--->$format")
            }

            val finalUri = URI.create(edtPrinterActivityEditUrl.text.toString())
            Log.i("printer", "finalUrl --- >$finalUri")
            printUtils.print(finalUri, file, this@PrintActivity, format)

            printUtils.getJobs(finalUri,this@PrintActivity);

            printUtils.getJobAttributes(finalUri,1,this@PrintActivity);

            printUtils.cancelJob(finalUri,1,this@PrintActivity);
        }

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
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            try {
                var printResponse: String = ""
                if (intent.getStringExtra("printResponse") != null) {
                    printResponse = intent.getStringExtra("printResponse").toString()
                    txtPrinterResponse.text = "Print Response - $printResponse"
                }

                var printerSupportedFormats: String = ""
                if (intent.getStringExtra("printerSupportedFormats") != null) {
                    printerSupportedFormats =
                        intent.getStringExtra("printerSupportedFormats").toString()
                    txtPrinterActivityFormatSupported.text =
                        "Printer Supported Format - $printerSupportedFormats"
                }

                var getPrinterAttributes: String = ""
                if (intent.getStringExtra("getPrinterAttributes") != null) {
                    getPrinterAttributes = intent.getStringExtra("getPrinterAttributes").toString()
                    txtDignosticInfo.text = "Get Attributes - $getPrinterAttributes"
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
            } catch (e: Exception) {
                txtDignosticInfo.text = e.toString()
            }
        }
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

        btnProceedPrint.setOnClickListener {
            var file = File(selectedFileString)
//            if (file.extension.toLowerCase() == "pdf") {
//                file = File("/storage/emulated/0/Movies/${file.nameWithoutExtension}.pclm")
//            } else {
//                file = File(selectedFileString)
//            }
            val inputFile = File(file.absolutePath)

            val fileName: String = inputFile.name
            var format: String? = null

            if (fileName.contains(".")) {
                format =
                    PrintUtils.extensionTypes[fileName.substring(fileName.lastIndexOf(".") + 1)]?.toLowerCase()
                        ?.trim()
                Log.i("printer", "format--->$format")
            }

            val finalUri = URI.create(edtPrinterActivityEditUrl.text.toString())
            Log.i("printer", "finalUrl --- >$finalUri")
            printUtils.print(finalUri, file, this@PrintActivity, format)
            dialog.cancel()
        }
    }


    private val DPI = 300
    private val IMAGE_TYPE = ImageType.RGB
    private val RED_COEFFICIENT = 0.2126
    private val GREEN_COEFFICIENT = 0.7512
    private val BLUE_COEFFICIENT = 0.0722

    var colorSpace = convertImageTypeToColorSpace(IMAGE_TYPE)

    private fun convertPDFtoPCL(uriFile: String) {

        val file = File(uriFile)
        val filePclName = "${file.nameWithoutExtension}.pclm"

        val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)

        val outputFormat = OutputFormat.toOutputFormat(getExtension(filePclName))

        val pdfInputStream: InputStream = BufferedInputStream(FileInputStream(File(uriFile)))

        val outputStream: OutputStream = BufferedOutputStream(
            FileOutputStream(File(path, "/$filePclName"))
        )

        Log.i("printer", "file path-- >$path")
        Log.i("printer", "outputStream--->$outputStream")

        try {
            CustomPDDocument.load(pdfInputStream).use { document ->
                val pdfRenderer = CustomPDFRenderer(document)
                val pages = document.pages
                val renderablePages: MutableList<RenderablePage> = ArrayList()

                Log.i("printer", "in covert method")

                for (pageIndex in 0 until pages.count) {
                    var bitmapFactory: BitmapFactory
//                    val bitmap = Bitmap.createBitmap(
//                        resources.displayMetrics.densityDpi * pages.getWidth() / 72,
//                        resources.displayMetrics.densityDpi * mCurrentPage.getHeight() / 72,
//                        Bitmap.Config.ARGB_8888
//                    )
                    val renderablePage: RenderablePage = object : RenderablePage(1001, 1001) {
                        override fun render(
                            yOffset: Int,
                            swathHeight: Int,
                            colorSpace: ColorSpace,
                            byteArray: ByteArray
                        ) {
                            val red = 0
                            val green = 13
                            val blue = 0
                            var rgb = 0
                            var byteIndex = 0
                            for (y in yOffset until yOffset + swathHeight) {
                                for (x in 0..566) {

//                                    rgb = image.getRGB(x, y)
//                                    red = (rgb >> 16) & 0xFF
//                                    green = (rgb > > 8) & 0xFF
//                                    blue = rgb & 0xFF
                                    if (colorSpace == ColorSpace.Grayscale) {
                                        byteArray[byteIndex++] =
                                            (RED_COEFFICIENT * red + GREEN_COEFFICIENT * green + BLUE_COEFFICIENT * blue).toByte()
                                    } else {
                                        byteArray[byteIndex++] = red.toByte()
                                        byteArray[byteIndex++] = green.toByte()
                                        byteArray[byteIndex++] = blue.toByte()
                                    }
                                }
                            }
                        }
                    }
                    renderablePages.add(renderablePage)
                }
                val renderableDocument: RenderableDocument = object : RenderableDocument() {
                    override fun iterator(): Iterator<RenderablePage> {
                        return renderablePages.iterator()
                    }

                    override val dpi: Int
                        get() = DPI
                }
                when (outputFormat) {
                    OutputFormat.PCLM -> saveRenderableDocumentAsPCLm(
                        renderableDocument,
                        colorSpace!!,
                        outputStream
                    )
                    OutputFormat.PWG_RASTER -> saveRenderableDocumentAsPWG(
                        renderableDocument,
                        colorSpace!!,
                        outputStream
                    )
                }
            }
        } catch (e: InvalidPasswordException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun convertImageTypeToColorSpace(imageType: ImageType): ColorSpace? {
        return when (imageType) {
            ImageType.BINARY, ImageType.GRAY -> ColorSpace.Grayscale
            else -> ColorSpace.Rgb
        }
    }

    @Throws(IOException::class)
    private fun saveRenderableDocumentAsPCLm(
        renderableDocument: RenderableDocument,
        colorSpace: ColorSpace, outputStream: OutputStream
    ) {
        val outputSettings =
            OutputSettings(colorSpace, Sides.oneSided, MediaSource.auto, null, false)
        val caps = PclmSettings(outputSettings, 32)
        val writer = PclmWriter(outputStream, caps)
        writer.write(renderableDocument)
        writer.close()
    }

    @Throws(IOException::class)
    private fun saveRenderableDocumentAsPWG(
        renderableDocument: RenderableDocument,
        colorSpace: ColorSpace, outputStream: OutputStream
    ) {
        val outputSettings =
            OutputSettings(colorSpace, Sides.oneSided, MediaSource.auto, null, false)
        val caps = PwgSettings(outputSettings)
        val writer = PwgWriter(outputStream, caps)
        writer.write(renderableDocument)
        writer.close()
    }

    private fun getExtension(name: String): String {
        val index = name.lastIndexOf(".")
        require(!(index == -1 || index <= name.lastIndexOf("/"))) { "$name has no extension" }
        return name.substring(index + 1)
    }

    enum class OutputFormat(val name1: String) {
        PWG_RASTER("pwg"),
        PCLM("PCLm");

        companion object {
            fun toOutputFormat(formatName: String): OutputFormat {
                for (format in values()) {
                    if (format.name1.equals(formatName, ignoreCase = true)) {
                        return format
                    }
                }
                throw IllegalArgumentException("Output format $formatName is invalid")
            }
        }
    }
}