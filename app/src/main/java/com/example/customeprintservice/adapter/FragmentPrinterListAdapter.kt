package com.example.customeprintservice.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
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
        holder.getCardview().setOnClickListener {
            it.setBackgroundColor(Color.GRAY)
        }
//        holder.bind(list?.get(position))
//
//        if (list?.get(position) is PrinterModel) {
//            val dataItem = list?.get(position) as PrinterModel
//            if (dataItem.isSelected) {
//                context?.let { ContextCompat.getColor(it, R.color.colorOrange) }
//                    ?.let { holder.getCardview().setBackgroundColor(it) }
//
//            } else {
//                context?.let { ContextCompat.getColor(it, R.color.white) }
//                    ?.let { holder.getCardview().setBackgroundColor(it) }
//
//            }
//        }
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

        fun getCardview(): CardView{
            return itemView.findViewById(R.id.cardviewFragmentPrinterList)
        }

        }
    }

