package com.example.customeprintservice.jipp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import kotlinx.android.synthetic.main.activity_printer_discovery.*
import org.jetbrains.anko.doAsync
import java.net.InetAddress
import java.net.URI

class PrinterDiscoveryActivity : AppCompatActivity() {

    var bundle = Bundle()
    var bundle1 =Bundle()
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

        val filter = IntentFilter()
        filter.addAction("com.example.CUSTOM_INTENT")
        val receiver = broadcastReceiver
        registerReceiver(receiver, filter)

        val filterSupportedFormat = IntentFilter()
        filterSupportedFormat.addAction("com.example.SUPPORTED_FORMAT")
        val receiverSupportedFormat = broadcastReceiverSupportedFormat
        registerReceiver(receiverSupportedFormat, filterSupportedFormat)
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

        btnAddPrinterManually.setOnClickListener {

            val printer: PrinterModel = PrinterModel()
            var inetAddress: InetAddress? = null
            doAsync {
                inetAddress = InetAddress.getByName(edtAddManualPrinter.text.toString())
            }
            Thread.sleep(100)
            printer.printerHost = inetAddress
            Log.i("printer","innet Address->"+inetAddress)
            printer.serviceName = inetAddress.toString()
            printer.printerPort = 631
            var flagIsExist: Boolean = false

            PrinterList().printerList.forEach {
                if (it.printerHost.equals(printer.printerHost)) {
                    flagIsExist = true
                }
            }

            if (!flagIsExist) {
                val boolean = PrinterList().addPrinterModel(printer)
                Toast.makeText(this@PrinterDiscoveryActivity, "Printer Added", Toast.LENGTH_SHORT)
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
            printerUri = URI.create(it.printerHost.toString())

            txtPrinterDiscoveryPrinterName.text = "Selected Printer -" + it.serviceName.toString()
            Log.i("printer", "publish subject --->$it")
            val uri = URI.create("http://$printerUri/ipp/print")
            val printerAttribute: String =
                attributesUtils.getAttributes(uri, this@PrinterDiscoveryActivity)
        }.subscribe()

        recyclerViewPrinterLst.adapter = adapter
    }


    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val ippPacket: String = intent.getStringExtra("getMessage").toString()
            Log.i("printer", "msg---->$ippPacket")
            try {
                bundle.putString("printerAttribute", ippPacket)
            } catch (e: Exception) {
            }
        }
    }
    var broadcastReceiverSupportedFormat: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val formatSupported = intent.getStringExtra("formatSupported").toString()
            Log.i("printer", "format Supported in broadcast receiver===>$formatSupported")
            bundle.putString("formatSupported", formatSupported)
        }
    }
}