package com.example.customeprintservice.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.R
import com.example.customeprintservice.jipp.PrinterModel


class FragmentPrinterListAdapter(
    val context: Context,
    val list: ArrayList<PrinterModel>
) : RecyclerView.Adapter<FragmentPrinterListAdapter.ViewHolder>() {
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
    }

}