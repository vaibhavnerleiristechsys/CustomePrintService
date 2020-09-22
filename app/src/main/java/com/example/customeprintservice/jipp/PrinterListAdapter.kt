package com.example.customeprintservice.jipp

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.R
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject

class PrinterListAdapter(val context: Context,
                         val list: List<PrinterModel>):RecyclerView.Adapter<PrinterListAdapter.ViewHolder>() {

    private var checkedRadioButton: CompoundButton? = null
    private val publishSubject: PublishSubject<String> = PublishSubject.create()

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

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: PrinterListAdapter.ViewHolder, position: Int) {

        holder.getPrinterName().text = ""+list[position].printerHost.toString()

        holder.getCardPrinterList().setOnClickListener {
            publishSubject.onNext(list[position].printerHost.toString())
        }
        holder.getRadioButton().setOnCheckedChangeListener(checkedChangeListener)
        if (holder.getRadioButton().isChecked) checkedRadioButton = holder.getRadioButton()

    }

    fun itemClick(): Observable<String> {
        return publishSubject.observeOn(AndroidSchedulers.mainThread())
    }
    private val checkedChangeListener = CompoundButton.OnCheckedChangeListener { compoundButton, isChecked ->
        checkedRadioButton?.apply { setChecked(!isChecked) }
        checkedRadioButton = compoundButton.apply { setChecked(isChecked) }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun getPrinterName(): TextView {
            return itemView.findViewById(R.id.txtPrinterName)
        }

        fun getRadioButton(): RadioButton {
            return itemView.findViewById(R.id.radioButton)
        }

        fun getCardPrinterList(): CardView{
            return itemView.findViewById(R.id.cardview)
        }
    }
}
