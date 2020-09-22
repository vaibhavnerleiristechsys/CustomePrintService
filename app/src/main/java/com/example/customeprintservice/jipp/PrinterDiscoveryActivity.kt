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
import kotlinx.android.synthetic.main.activity_printer_discovery.*
import org.jetbrains.anko.doAsync
import java.net.InetAddress

class PrinterDiscoveryActivity : AppCompatActivity() {
    var bundle = Bundle()

    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer_discovery)

        val actionBar = supportActionBar
        actionBar?.title = "Printer Discovery"
        actionBar?.setDisplayHomeAsUpEnabled(true)

        bundle = intent.extras!!

        val selectedFile: String? = bundle.getString("selectedFile")

        Log.i("printer", "selected file in printer discovery activty$selectedFile")
        btnSelectPrinter.setOnClickListener {
            dialogPrinterList()
        }

        btnNextPrinterDiscovery.setOnClickListener {
            val intent = Intent(this@PrinterDiscoveryActivity, PrintActivity::class.java)
            startActivity(intent)
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

        btnAddPrinterManually.setOnClickListener {

            val printer: PrinterModel = PrinterModel()
            var inetAddress: InetAddress? = null
            doAsync {
                inetAddress = InetAddress.getByName(edtAddManualPrinter.toString())
            }
            Thread.sleep(100)
            printer.printerHost = inetAddress
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
            Log.i("printer", "publish subject --->$it")
        }.subscribe()

        recyclerViewPrinterLst.adapter = adapter
    }
}