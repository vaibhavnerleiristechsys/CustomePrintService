package com.printerlogic.printerlogic.adapter

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import com.printerlogic.printerlogic.R

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
                Toast.makeText(context,"   "+list[position].toString() , Toast.LENGTH_LONG).show()
            }

                val intent = Intent("moveRecyclerView")
                intent.putExtra("Character", list[position])
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent)

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
