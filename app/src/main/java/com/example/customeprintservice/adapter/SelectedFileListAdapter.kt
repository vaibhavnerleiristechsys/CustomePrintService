package com.example.customeprintservice.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

class SelectedFileListAdapter(
    val context: Context,
    val list: ArrayList<String>
) : RecyclerView.Adapter<SelectedFileListAdapter.ViewHolder>() {

    private var checkedRadioButton: CompoundButton? = null
    private val publishSubject: PublishSubject<String> = PublishSubject.create()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SelectedFileListAdapter.ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.card_selected_file_list, parent, false)
        return SelectedFileListAdapter.ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.getSelectedFileName().text = list[position]

        holder.getRadioButton().setOnCheckedChangeListener(checkedChangeListener)
        if (holder.getRadioButton().isChecked) checkedRadioButton = holder.getRadioButton()

        holder.getRadioButton().setOnClickListener {
            publishSubject.onNext(list[position])
            removeAt(position)
        }
    }

    fun itemClick(): Observable<String> {
        return publishSubject.observeOn(AndroidSchedulers.mainThread())
    }

     fun removeAt(position: Int) {
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, list.size)
    }

    private val checkedChangeListener =
        CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
            checkedRadioButton?.apply { setChecked(!isChecked) }
            checkedRadioButton = compoundButton.apply { setChecked(isChecked) }
        }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        fun getSelectedFileName(): TextView {
            return itemView.findViewById(R.id.txtSelectedFile)
        }

        fun getRadioButton(): RadioButton {
            return itemView.findViewById(R.id.radioButtonSelectFile)
        }
    }
}