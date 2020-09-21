package com.example.customeprintservice.jipp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.R

class PrinterListAdapter(val context: Context,
                         val list: List<String>):RecyclerView.Adapter<PrinterListAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PrinterListAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_printer_list, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: PrinterListAdapter.ViewHolder, position: Int) {
        holder.getPrinterName().text = list[position]
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun getPrinterName(): TextView {
            return itemView.findViewById(R.id.txtPrinterName)
        }

    }
}