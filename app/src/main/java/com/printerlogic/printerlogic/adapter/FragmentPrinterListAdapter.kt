package com.printerlogic.printerlogic.adapter

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.preference.PreferenceManager
import android.util.Log
import android.view.*
import android.widget.AbsListView
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.printerlogic.printerlogic.R
import com.printerlogic.printerlogic.jipp.PrinterList
import com.printerlogic.printerlogic.jipp.PrinterModel
import com.printerlogic.printerlogic.model.DecodedJWTResponse
import com.printerlogic.printerlogic.prefs.LoginPrefs
import com.printerlogic.printerlogic.prefs.SignInCompanyPrefs
import com.printerlogic.printerlogic.print.BottomNavigationActivityForServerPrint
import com.printerlogic.printerlogic.print.PrintersFragment
import com.printerlogic.printerlogic.print.ServerPrintRelaseFragment
import com.printerlogic.printerlogic.printjobstatus.PrinterListService
import com.printerlogic.printerlogic.utils.JwtDecode
import com.printerlogic.printerlogic.utils.ProgressDialog
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.function.Consumer


class FragmentPrinterListAdapter (
    val context: Context,
    val list: ArrayList<PrinterModel>,
    val location: String


) : RecyclerView.Adapter<FragmentPrinterListAdapter.ViewHolder>() {
    val holders = ArrayList<ViewHolder>()
    private var selectedPosition = -1
    val headers = ArrayList<String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FragmentPrinterListAdapter.ViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.card_fragment_printer_list, parent, false)
        return FragmentPrinterListAdapter.ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: FragmentPrinterListAdapter.ViewHolder, position: Int) {
        holder.getPrinterName().text = list[position].serviceName.toString()
        holder.getRemovePrinter().visibility=View.GONE
        if(list[position].manual==true){
            holder.getRemovePrinter().visibility=View.VISIBLE
        }
        if(list[position].location !="" && list[position].location != null){
            holder.getBuildingName().text = list[position].location.toString()
        }else{
            holder.getBuildingName().text ="-"
        }

        if(list[position].isColor==0){
            holder.getPrinterImageIcon().visibility=View.VISIBLE
            holder.getColorPrinterImageIcon().visibility=View.GONE
        }else if(list[position].isColor==0){
            holder.getPrinterImageIcon().visibility=View.GONE
            holder.getColorPrinterImageIcon().visibility=View.VISIBLE
        }

       if(location.equals("printerTab") || location.equals("printpreview")){
            holder.getPrinterHeaderName().visibility=View.GONE
        }else{
            holder.getPrinterHeaderName().visibility=View.VISIBLE
        }


     //  holder.getPrinterHeaderName().visibility=View.VISIBLE
        var isExist= false


            for (header in headers) {
                if(header.equals("Recent") && list[position].recentUsed==true){
                    isExist = true;
                }
                else if (header.equals(list[position].serviceName.get(0).toString().toLowerCase())) {
                    isExist = true;
                }
            }
            if (isExist == false) {
                if(list[position].recentUsed==true){
                    headers.add("Recent");
                    holder.getPrinterHeaderName().setBackgroundColor(Color.parseColor("#F1F2F3"))
                    holder.getPrinterHeaderName().text="Recent"
                }else {
                    headers.add(list[position].serviceName.get(0).toString().toLowerCase())
                    holder.getPrinterHeaderName().setBackgroundColor(Color.parseColor("#F1F2F3"))
                    holder.getPrinterHeaderName().text =
                        list[position].serviceName.get(0).toString().toUpperCase();
                }
            }
            if (isExist == true) {
                holder.getPrinterHeaderName().visibility = View.GONE
            }







        holders.add(holder)
        this.selectedPosition = position
        if(location.equals("selectPrinter") || location.equals("printpreview")) {

        holder.getCardview().setOnClickListener {
            if(location.equals("printpreview")) {
                if (list[position].printerHost != null) {
                    val intent = Intent("selected print preview printer")
                    intent.putExtra("printer name", list[position].serviceName);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

                }

            } else {
                if (list[position].printerHost != null) {
                    if (list[position].recentUsed != true) {
                        addRecentPrintersToPref(list[position])
                    }
                    Log.d("selected printerdetails", list[position].serviceName.toString())
                    Log.d("selected printerdetails", list[position].printerHost.toString())
                    BottomNavigationActivityForServerPrint.selectedPrinter.serviceName =
                        list[position].serviceName.toString()
                    BottomNavigationActivityForServerPrint.selectedPrinter.printerHost =
                        list[position].printerHost
                    BottomNavigationActivityForServerPrint.selectedPrinter.id = list[position].id

                    if (list[position].id != null) {
                        PrintersFragment().getPrinterListByPrinterId(
                            context,
                            list[position].id.toString(),
                            "getprinterToken"
                        )
                    }

                    if (list[position].manual == true) {
                        var finalLocalurl =
                            "http" + "://" + list[position].printerHost.toString() + ":631/ipp/print"
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
        }


        holder.getRemovePrinter().setOnClickListener {
            Log.d("printer name tag:", holder.getPrinterName().text.toString())
           list.forEach{

                try {
                    if (it.serviceName.equals(holder.getPrinterName().text.toString())) {
                        Log.d("delete dialog open","delete dialog open")
                        dialogDeletePrinter( it)
                        Log.d("after delete dialog","after delete dialog")
                    }
                }catch (e: Exception){
                    Log.d("excpetion", e.message.toString())
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun getPrinterName(): TextView {
            return itemView.findViewById(R.id.txtFragmentPrinterName)
        }

        fun getPrinterHeaderName(): TextView {
            return itemView.findViewById(R.id.textForHeader)
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

        fun getPrinterImageIcon(): ImageView {
            return itemView.findViewById(R.id.imgPrintImage)
        }

        fun getColorPrinterImageIcon(): ImageView {
            return itemView.findViewById(R.id.imgColorPrintImage)
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



    fun addRecentPrintersToPref(printerModel: PrinterModel){
        var recentUsedPrinters = java.util.ArrayList<PrinterModel>()
        val prefs1 = PreferenceManager.getDefaultSharedPreferences(context)
        val gson1 = Gson()
        val json2 = prefs1.getString("recentUsedPrinters", null)
        val type1 = object :
            TypeToken<java.util.ArrayList<PrinterModel?>?>() {}.type
        if (json2 != null) {
            recentUsedPrinters = gson1.fromJson<java.util.ArrayList<PrinterModel>>(
                json2,
                type1
            )
            if(recentUsedPrinters.size<4) {
                recentUsedPrinters.add(0, printerModel)
            }else{
                recentUsedPrinters.removeAt(3)
                recentUsedPrinters.add(0, printerModel)
            }
            val editor = prefs1.edit()
            val json1 = gson1.toJson(recentUsedPrinters)
            editor.putString("recentUsedPrinters", json1)
            editor.apply()

        }else{
            recentUsedPrinters.add(0, printerModel)
            val editor = prefs1.edit()
            val json1 = gson1.toJson(recentUsedPrinters)
            editor.putString("recentUsedPrinters", json1)
            editor.apply()
        }
    }



    @SuppressLint("WrongConstant")
    @RequiresApi(api = Build.VERSION_CODES.N)
    fun dialogDeletePrinter(printerModel: PrinterModel){
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
                        if (it.serviceName.equals(printerModel.serviceName)) {
                            printer= it
                        }
                    }catch (e: Exception){
                        Log.d("excpetion", e.message.toString())
                    }
                }
                PrinterList().printerList.remove(printer)


                removePrinterFromSharedDocumentPrinterList(printerModel)
                removePrinterFromPrinterTabList(printerModel)

            }

            dialog.cancel()
            //  notifyDataSetChanged()
            val intent = Intent("callUpdateUIMethod")
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

        }

        cancel.setOnClickListener {
            dialog.cancel()
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun removePrinterFromSharedDocumentPrinterList(printerModel: PrinterModel){
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val gson = Gson()
        val json = prefs.getString("prefServerSecurePrinterListWithDetails", null)
        val type = object :
            TypeToken<java.util.ArrayList<PrinterModel?>?>() {}.type
        var sharedPreferencesStoredPrinterListWithDetails = java.util.ArrayList<PrinterModel>()
        if (json != null) {
            sharedPreferencesStoredPrinterListWithDetails = gson.fromJson<java.util.ArrayList<PrinterModel>>(
                json,
                type
            )
        }

        if (sharedPreferencesStoredPrinterListWithDetails != null && sharedPreferencesStoredPrinterListWithDetails.size > 0) {
            var removePrinter: PrinterModel = PrinterModel()
            sharedPreferencesStoredPrinterListWithDetails.forEach(Consumer { p: PrinterModel ->
                if (p.serviceName.equals(printerModel.serviceName)) {
                    removePrinter = p
                }
            })
            if(removePrinter !=null) {
                sharedPreferencesStoredPrinterListWithDetails.remove(removePrinter)
                val editor = prefs.edit()
                val json1 = gson.toJson(sharedPreferencesStoredPrinterListWithDetails)
                editor.putString("prefServerSecurePrinterListWithDetails", json1)
                editor.apply()
            }
            //  list.addAll(sharedPreferencesStoredPrinterListWithDetails)
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun removePrinterFromPrinterTabList(printerModel: PrinterModel){
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val gson = Gson()
        val json = prefs.getString("prefaddedPrinterListWithDetails", null)
        val type = object :
            TypeToken<java.util.ArrayList<PrinterModel?>?>() {}.type
        var sharedPreferencesStoredPrinterListWithDetails = java.util.ArrayList<PrinterModel>()
        if (json != null) {
            sharedPreferencesStoredPrinterListWithDetails = gson.fromJson<java.util.ArrayList<PrinterModel>>(
                json,
                type
            )
        }

        if (sharedPreferencesStoredPrinterListWithDetails != null && sharedPreferencesStoredPrinterListWithDetails.size > 0) {
            var removePrinter: PrinterModel = PrinterModel()
            sharedPreferencesStoredPrinterListWithDetails.forEach(Consumer { p: PrinterModel ->
                if (p.serviceName.equals(printerModel.serviceName)) {
                    removePrinter = p
                }
            })
            if(removePrinter !=null) {
                sharedPreferencesStoredPrinterListWithDetails.remove(removePrinter)
                val editor = prefs.edit()
                val json1 = gson.toJson(sharedPreferencesStoredPrinterListWithDetails)
                editor.putString("prefaddedPrinterListWithDetails", json1)
                editor.apply()
            }
            //  list.addAll(sharedPreferencesStoredPrinterListWithDetails)
        }

    }
}

