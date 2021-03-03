package com.example.customeprintservice.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.*
import android.widget.AbsListView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.R
import com.example.customeprintservice.jipp.PrinterList
import com.example.customeprintservice.jipp.PrinterModel
import com.example.customeprintservice.model.DecodedJWTResponse
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.prefs.SignInCompanyPrefs
import com.example.customeprintservice.print.PrintersFragment
import com.example.customeprintservice.print.ServerPrintRelaseFragment
import com.example.customeprintservice.printjobstatus.PrinterListService
import com.example.customeprintservice.utils.JwtDecode
import com.example.customeprintservice.utils.ProgressDialog
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue


class FragmentPrinterListAdapter(
    val context: Context,
    val list: ArrayList<PrinterModel>,
    val location:String



) : RecyclerView.Adapter<FragmentPrinterListAdapter.ViewHolder>() {
    val holders = ArrayList<ViewHolder>()
    private var selectedPosition = -1


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FragmentPrinterListAdapter.ViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.card_fragment_printer_list, parent, false)
        return FragmentPrinterListAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: FragmentPrinterListAdapter.ViewHolder, position: Int) {
        holder.getPrinterName().text = list[position].serviceName.toString()
        holder.getRemovePrinter().visibility=View.GONE
        if(list[position].manual==true){
            holder.getRemovePrinter().visibility=View.VISIBLE
        }
        holders.add(holder)
        this.selectedPosition = position
        if(location.equals("selectPrinter")) {
        holder.getCardview().setOnClickListener {
            if (list[position].printerHost != null) {
                Log.d("selected printerdetails", list[position].serviceName.toString())
                Log.d("selected printerdetails", list[position].printerHost.toString())

                if (list[position].id != null) {
                    PrintersFragment().getPrinterListByPrinterId(
                        context,
                        list[position].id.toString(),
                        "getprinterToken"
                    )
                }

                if (list[position].manual == true) {
                    var finalLocalurl = "http" + "://" + list[position].printerHost.toString() + ":631/ipp/print"
                    ServerPrintRelaseFragment.localPrinturl = finalLocalurl
                } else {

                    ProgressDialog.showLoadingDialog(context, "Getting Printer Details")

                    if (list[position].nodeId != null) {
                        PrinterListService().getPrinterDetails(
                            context,
                            LoginPrefs.getOCTAToken(context).toString(),
                            decodeJWT(),
                            SignInCompanyPrefs.getIdpType(context).toString(),
                            SignInCompanyPrefs.getIdpName(context).toString(),
                            list[position].nodeId.toString(),
                            false
                        )
                    }
                }
            }
            val intent = Intent("message_subject_intent")
            intent.putExtra("name", "message")
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

            for (i in holders.indices) {
                val holder = holders[i]
                if (i == position) {
                    if (location.equals("selectPrinter")) {
                        holder.getCardview().setCardBackgroundColor(Color.GRAY)
                    }
                } else {
                    holder.getCardview().setCardBackgroundColor(Color.WHITE)
                }
            }
        }
        }


        holder.getRemovePrinter().setOnClickListener {
              dialogDeletePrinter(context,list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun getPrinterName(): TextView {
            return itemView.findViewById(R.id.txtFragmentPrinterName)
        }

        fun getBuildingName(): TextView {
            return itemView.findViewById(R.id.txtFragmentBuildingName)
        }

        fun getCardview(): CardView {
            return itemView.findViewById(R.id.cardviewFragmentPrinterList)
        }
        fun getRemovePrinter(): ImageView {
            return itemView.findViewById(R.id.removePrinter)
        }



    }


    private fun decodeJWT(): String {
        var userName: String? = null
        try {
            val mapper = jacksonObjectMapper()
            val decoded: DecodedJWTResponse = mapper.readValue<DecodedJWTResponse>(
                LoginPrefs.getOCTAToken(context)?.let { JwtDecode.decoded(it) }!!
            )
            userName = decoded.user.toString()
        } catch (ex: Exception) {

        }
        return userName.toString()
    }


    @SuppressLint("WrongConstant")
    fun dialogDeletePrinter(context:Context,printerModel: PrinterModel){
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.dialog_confirmation_delete_printer)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        val window = dialog.window
        window!!.setLayout(
            AbsListView.LayoutParams.WRAP_CONTENT,
            AbsListView.LayoutParams.WRAP_CONTENT
        )
        val wlp = window.attributes
        wlp.gravity = Gravity.CENTER
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(0.5f)
        window.attributes = wlp
        val button = dialog.findViewById<Button>(R.id.ok)
        val cancel = dialog.findViewById<Button>(R.id.cancel)
        dialog.show()

        button.setOnClickListener {
            if(printerModel.serviceName != null) {
                var printer: PrinterModel = PrinterModel()
                PrinterList().printerList.forEach{

                    try {
                        if (it.printerHost.equals(printerModel.printerHost)) {
                            printer= it
                        }
                    }catch(e: Exception){
                        Log.d("excpetion",e.message.toString())
                    }
                }
                PrinterList().printerList.remove(printer)
            }

            dialog.cancel()
            notifyDataSetChanged()

        }

        cancel.setOnClickListener {
            dialog.cancel()
        }

    }
}

