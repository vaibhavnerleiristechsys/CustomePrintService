package com.example.customeprintservice.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.R

class FragmentSelectedFileListAdapter(
    val context: Context,
    val list: ArrayList<String>
) : RecyclerView.Adapter<FragmentSelectedFileListAdapter.ViewHolder>() {

    private lateinit var listener: ViewHolder.FragmentSelectedFileAdapterListener
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FragmentSelectedFileListAdapter.ViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.card_fragment_selected_file_list, parent, false)
        return FragmentSelectedFileListAdapter.ViewHolder(view)
    }

    fun setListener(listener: ViewHolder.FragmentSelectedFileAdapterListener) {
        this.listener = listener
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: FragmentSelectedFileListAdapter.ViewHolder,
        position: Int
    ) {

        holder.getFileName().text = list[position]

        holder.getFileCardView().setOnLongClickListener {
           listener.onItemLongClick(position)
            return@setOnLongClickListener true
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun getFileName(): TextView {
            return itemView.findViewById(R.id.txtFileName)
        }

        fun getFileSize(): TextView {
            return itemView.findViewById(R.id.txtFileSize)
        }

        fun getSelectedDate(): TextView {
            return itemView.findViewById(R.id.txtFileTimeDate)
        }

        fun getFileCardView(): CardView {
            return itemView.findViewById(R.id.cardviewFragmentSelectedFileList)
        }

        interface FragmentSelectedFileAdapterListener {
            fun onItemClick(position: Int)
            fun onItemLongClick(position: Int)
        }
    }
}