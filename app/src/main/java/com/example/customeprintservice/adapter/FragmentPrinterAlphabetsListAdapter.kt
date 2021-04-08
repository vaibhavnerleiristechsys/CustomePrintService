package com.example.customeprintservice.adapter

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
import com.example.customeprintservice.R
import com.example.customeprintservice.jipp.PrinterList
import com.example.customeprintservice.jipp.PrinterModel
import com.example.customeprintservice.model.DecodedJWTResponse
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.prefs.SignInCompanyPrefs
import com.example.customeprintservice.print.BottomNavigationActivityForServerPrint
import com.example.customeprintservice.print.PrintersFragment
import com.example.customeprintservice.print.ServerPrintRelaseFragment
import com.example.customeprintservice.printjobstatus.PrinterListService
import com.example.customeprintservice.utils.JwtDecode
import com.example.customeprintservice.utils.ProgressDialog
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FragmentPrinterAlphabetsListAdapter (
    val context: Context,
    val list: ArrayList<String>

) : RecyclerView.Adapter<FragmentPrinterAlphabetsListAdapter.ViewHolder>() {
    val holders = ArrayList<ViewHolder>()
    private var selectedPosition = -1
    val headers = ArrayList<String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FragmentPrinterAlphabetsListAdapter.ViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.alphabets_printerlist, parent, false)
        return FragmentPrinterAlphabetsListAdapter.ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: FragmentPrinterAlphabetsListAdapter.ViewHolder, position: Int) {

        holder.getAlphabetsName().text = list[position].toString()

        holder.getAlphabetsName().setOnClickListener {
            if (list[position] != null) {
           Log.d("alphabets:",list[position])
            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun getAlphabetsName(): TextView {
            return itemView.findViewById(R.id.alphabetsText)
        }

    }

}
