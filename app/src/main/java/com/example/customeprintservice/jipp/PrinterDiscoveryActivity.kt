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
    @SuppressLint("WrongConstant")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_printer_discovery)

        val actionBar = supportActionBar
        actionBar?.title = "Printer Discovery"
        actionBar?.setDisplayHomeAsUpEnabled(true)

        btnSelectPrinter.setOnClickListener {

            PrintUtils().setContextAndInitializeJMDNS(this@PrinterDiscoveryActivity)
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
                inetAddress = InetAddress.getLocalHost()
            }
            Thread.sleep(100)
            printer.printerHost = inetAddress
            printer.serviceName = "inetAddress?.hostAddress.toString()"
            printer.printerPort = 631


            val boolean = PrinterList().addPrinterModel(printer)
            if (boolean) {
                Toast.makeText(this@PrinterDiscoveryActivity, "Printer Added", Toast.LENGTH_SHORT)
                    .show()
                dialog.dismiss()
            } else {
                Toast.makeText(
                    this@PrinterDiscoveryActivity,
                    "Unable to add Printer",
                    Toast.LENGTH_SHORT
                ).show()
            }
            Log.i("printer", "flag-->$boolean")
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
        recyclerViewPrinterLst.adapter = adapter
    }
}