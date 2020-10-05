package com.example.customeprintservice.jipp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.R
import com.example.customeprintservice.adapter.PrinterListAdapter
import com.example.customeprintservice.utils.Inet
import kotlinx.android.synthetic.main.activity_printer_discovery.*
import org.jetbrains.anko.doAsync
import java.net.InetAddress
import java.net.URI

class PrinterDiscoveryActivity : AppCompatActivity() {

    var bundle = Bundle()
    var bundle1 = Bundle()
    var attributesUtils = AttributesUtils()
    var printerUri: URI? = null

    @SuppressLint("WrongConstant", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer_discovery)

        val actionBar = supportActionBar
        actionBar?.title = "Printer Discovery"
        actionBar?.setDisplayHomeAsUpEnabled(true)

        bundle = intent.extras!!

        if (bundle.getString("selectedFile") != null) {
            val selectedFile: String? = bundle.getString("selectedFile")
            bundle.putString("selectedFile", bundle.getString("selectedFile"))
            bundle.putStringArrayList(
                "selectedFileList",
                bundle.getStringArrayList("selectedFileList")
            )
            txtPrinterDiscoverySelectedDocument.text =
                "Selected Document -${selectedFile.toString()}"
        }

        btnSelectPrinter.setOnClickListener {
            dialogPrinterList()
        }


        btnNextPrinterDiscovery.setOnClickListener {
            if (printerUri != null && bundle.getString("selectedFile") != null) {
                val intent = Intent(this@PrinterDiscoveryActivity, PrintActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            } else {
                Toast.makeText(
                    this@PrinterDiscoveryActivity,
                    "Please select Printer",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        edtAddManualPrinter.setOnClickListener {
            dialogAddManualPrinter()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun dialogAddManualPrinter() {
        val dialog = Dialog(this@PrinterDiscoveryActivity)
        dialog.setContentView(R.layout.dialog_add_manual_printer)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        dialog.window?.setLayout((6 * width) / 7, WindowManager.LayoutParams.WRAP_CONTENT)

        val edtAddManualPrinter = dialog.findViewById<EditText>(R.id.edtDialogAddManualPrinter)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnAddPrinterManually = dialog.findViewById<Button>(R.id.btnAddPrinter)
        btnCancel.setOnClickListener { dialog.dismiss() }

        val printer: PrinterModel = PrinterModel()

        btnAddPrinterManually.setOnClickListener {
            if (Inet.validIP(edtAddManualPrinter.text.toString())) {
                try {
                    var inetAddress: InetAddress? = null

                    doAsync {
                        inetAddress = InetAddress.getByName(edtAddManualPrinter.text.toString())
                    }
                    Thread.sleep(100)
                    if (inetAddress != null) {
                        printer.printerHost = inetAddress
                        printer.serviceName = "" + inetAddress
                        printer.printerPort = 631
                        Log.i("printer", "innet Address->" + inetAddress)
                    }

                } catch (e: Exception) {
                    Log.i("printer", e.toString())
                }

                var flagIsExist: Boolean = false

                PrinterList().printerList.forEach {
                    if (it.printerHost.equals(printer.printerHost)) {
                        flagIsExist = true
                    }
                }

                if (!flagIsExist) {
                    val boolean = PrinterList().addPrinterModel(printer)
                    Toast.makeText(
                        this@PrinterDiscoveryActivity,
                        "Printer Added",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    dialog.dismiss()
                    Log.i("printer", "flag-->$boolean")
                } else {
                    Toast.makeText(
                        this@PrinterDiscoveryActivity,
                        "Unable to add Printer",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this@PrinterDiscoveryActivity, "IP is not valid", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }


    @SuppressLint("WrongConstant")
    private fun dialogPrinterList() {
        val dialog = Dialog(this@PrinterDiscoveryActivity)
        dialog.setContentView(R.layout.dialog_printer_list)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()

        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        dialog.window?.setLayout((6 * width) / 7, WindowManager.LayoutParams.WRAP_CONTENT)

        val recyclerViewPrinterLst = dialog.findViewById<RecyclerView>(R.id.recyclerViewPrinterList)
        val btnPrinterListDialogCancel = dialog.findViewById<Button>(R.id.btnCancelPrinterlistPopUp)
        btnPrinterListDialogCancel.setOnClickListener {
            dialog.dismiss()
        }

        recyclerViewPrinterLst.layoutManager =
            LinearLayoutManager(
                this@PrinterDiscoveryActivity,
                LinearLayout.VERTICAL,
                false
            )

        val adapter = PrinterListAdapter(
            this@PrinterDiscoveryActivity,
            PrinterList().printerList
        )
        adapter.itemClick().doOnNext {
            bundle.putString("ipAddress", it.printerHost.toString())
            bundle.putString("printerName", it.serviceName.toString())
            bundle.putString("printerPort", it.printerPort.toString())
            printerUri = URI.create(it.printerHost.toString())

            txtPrinterDiscoveryPrinterName.text = "Selected Printer -" + it.serviceName.toString()
            Log.i("printer", "publish subject --->$it")
//            val uri = URI.create("http://${printerUri}:${it.printerPort}/ipp/print")
//            val printerAttribute: String =
//                attributesUtils.getAttributes(uri, this@PrinterDiscoveryActivity)
        }.subscribe()

        recyclerViewPrinterLst.adapter = adapter
    }

}