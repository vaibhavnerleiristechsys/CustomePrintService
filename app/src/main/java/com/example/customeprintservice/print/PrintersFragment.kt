package com.example.customeprintservice.print

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.R
import com.example.customeprintservice.adapter.FragmentPrinterListAdapter
import com.example.customeprintservice.jipp.PrinterList
import com.example.customeprintservice.jipp.PrinterModel
import com.example.customeprintservice.utils.Inet
import kotlinx.android.synthetic.main.fragment_printers.*
import org.jetbrains.anko.doAsync
import java.net.InetAddress

class PrintersFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_printers, container, false)
    }

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnAddManuallyPrinter.setOnClickListener {
            dialogAddManualPrinter()
        }
        updateUi()
        Log.i("printer","Login octa token"+LoginPrefs.getOCTAToken(requireContext()))
    }

    @SuppressLint("WrongConstant")
    private fun updateUi() {
        val recyclerViewPrinterLst =
            view?.findViewById<RecyclerView>(R.id.recyclerViewFragmentPrinterList)

        recyclerViewPrinterLst?.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayout.VERTICAL,
                false
            )
        val adapter = FragmentPrinterListAdapter(
            context as Activity,
            PrinterList().printerList
        )
        recyclerViewPrinterLst?.adapter = adapter
    }

    @SuppressLint("WrongConstant")
    private fun dialogAddManualPrinter() {
        val dialog = Dialog(context as Activity)
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
                    PrinterList().addPrinterModel(printer)
                    Toast.makeText(context, "Printer Added", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    updateUi()
                } else {
                    Toast.makeText(context, "Unable to add Printer", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "IP is not valid", Toast.LENGTH_SHORT).show()
            }
        }
    }
}