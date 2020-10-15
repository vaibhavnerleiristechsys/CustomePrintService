package com.example.customeprintservice.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.R
import com.example.customeprintservice.model.FileAttributes
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import org.w3c.dom.Text

class FragmentSelectedFileListAdapter(
    val context: Context,
    val list: ArrayList<FileAttributes>
) : RecyclerView.Adapter<FragmentSelectedFileListAdapter.ViewHolder>() {

    private var checkedRadioButton: CompoundButton? = null
    private val publishSubject: PublishSubject<String> = PublishSubject.create()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FragmentSelectedFileListAdapter.ViewHolder {
        val view =
            LayoutInflater.from(context)
                .inflate(R.layout.card_fragment_selected_file_list, parent, false)
        return FragmentSelectedFileListAdapter.ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: FragmentSelectedFileListAdapter.ViewHolder,
        position: Int
    ) {
        holder.getFileName().text = list[position].fileName
        holder.getFileSize().text = list[position].fileSize.toString()+"KB"
        holder.getSelectedDate().text = list[position].fileSelectedDate.toString()
    }

    fun itemClick(): Observable<String> {
        return publishSubject.observeOn(AndroidSchedulers.mainThread())
    }

    fun removeAt(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    private val checkedChangeListener =
        CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
            checkedRadioButton?.apply { setChecked(!isChecked) }
            checkedRadioButton = compoundButton.apply { setChecked(isChecked) }
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
    }
}