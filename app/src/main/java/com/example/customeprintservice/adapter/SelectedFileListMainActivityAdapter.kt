package com.example.customeprintservice.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.R

class SelectedFileListMainActivityAdapter(
    val context: Context,
    val list: ArrayList<String>
) : RecyclerView.Adapter<SelectedFileListMainActivityAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedFileListMainActivityAdapter.ViewHolder {
        val view = LayoutInflater.from(context)
                .inflate(R.layout.card_selected_file_list_main_activity, parent, false)
        return SelectedFileListMainActivityAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: SelectedFileListMainActivityAdapter.ViewHolder, position: Int) {
        holder.getSelectedFileName().text = list[position]
        holder.getRemoveItem().setOnClickListener {
            removeAt(position)
        }
    }

    fun removeAt(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun getSelectedFileName(): TextView {
            return itemView.findViewById(R.id.txtSelectedFileMainActivity)
        }

        fun getRemoveItem(): Button {
            return itemView.findViewById(R.id.btnRemoveFromList)
        }
    }
}