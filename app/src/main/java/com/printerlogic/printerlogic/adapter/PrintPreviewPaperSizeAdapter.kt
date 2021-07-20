package com.printerlogic.printerlogic.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.printerlogic.printerlogic.R



class PrintPreviewPaperSizeAdapter (
    val context: Context,
    val list: ArrayList<String>

) : RecyclerView.Adapter<PrintPreviewPaperSizeAdapter.ViewHolder>() {
    val holders = ArrayList<ViewHolder>()
    private var selectedPosition = -1
    val headers = ArrayList<String>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PrintPreviewPaperSizeAdapter.ViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.card_fragment_printer_paper_size, parent, false)
        return PrintPreviewPaperSizeAdapter.ViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onBindViewHolder(holder: PrintPreviewPaperSizeAdapter.ViewHolder, position: Int) {
        holder.getPrinterSizeName().text = list[position].toString()




        holders.add(holder)
        this.selectedPosition = position

            holder.getCardview().setOnClickListener {

                Log.d("Paper Size",list[position].toString())
                        val intent = Intent("selected print preview paper size")
                        intent.putExtra("printer paper size", list[position].toString());
                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

            }



    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun getPrinterSizeName(): TextView {
            return itemView.findViewById(R.id.txtFragmentPrinterSizeName)
        }

        fun getCardview(): CardView {
            return itemView.findViewById(R.id.cardviewFragmentPrinterPaperSizeList)
        }

    }



}

