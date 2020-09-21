package com.example.customeprintservice.jipp

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.LinearLayout
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
    }


    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
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

        var list = ArrayList<PrinterModel>()
        val printer = PrinterModel()
        var inetAddress: InetAddress? = null
        doAsync {
            inetAddress= InetAddress.getLocalHost()
        }
        Thread.sleep(100)

//        list= PrinterList.getPrinterList() as ArrayList<Printer>

        printer.printerHost = inetAddress
        printer.printerPort = 630
        printer.serviceName = "23234"

        list.add(printer)
        list.add(printer)
        list.add(printer)

        recyclerViewPrinterLst.layoutManager =
            LinearLayoutManager(
                this@PrinterDiscoveryActivity,
                LinearLayout.VERTICAL,
                false
            )

        val adapter = PrinterListAdapter(
            this@PrinterDiscoveryActivity,
            list
        )

        recyclerViewPrinterLst.adapter = adapter
    }
}