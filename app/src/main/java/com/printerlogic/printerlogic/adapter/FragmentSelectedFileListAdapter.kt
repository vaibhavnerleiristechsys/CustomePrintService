package com.printerlogic.printerlogic.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.printerlogic.printerlogic.R
import com.printerlogic.printerlogic.room.SelectedFile
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import java.util.*


class FragmentSelectedFileListAdapter(
    val context: Context,
    val list: ArrayList<SelectedFile>?
) : RecyclerView.Adapter<FragmentSelectedFileListAdapter.ViewHolder>() {

    private lateinit var listener: ViewHolder.FragmentSelectedFileAdapterListener
    private val publishSubject: PublishSubject<SelectedFile> = PublishSubject.create()
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

    fun itemClick(): Observable<SelectedFile> {
        return publishSubject.observeOn(AndroidSchedulers.mainThread())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: FragmentSelectedFileListAdapter.ViewHolder, position: Int) {
        holder.getFileName().text = "" + list!![position].fileName
        holder.getSelectedDate().text = "" + list[position].fileSelectedDate
        holder.getFileSize().text = "" + ((list[position].filePath)?.length)
        holder.getCheckbox().setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                publishSubject.onNext(list[position])
            }
        }

    }

    override fun getItemCount(): Int {
        return list?.size!!
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

        fun getCheckbox(): CheckBox {
            return itemView.findViewById(R.id.checkbox)
        }

        interface FragmentSelectedFileAdapterListener {
            fun onItemClick(position: Int)
            fun onItemLongClick(position: Int)
        }
    }
}