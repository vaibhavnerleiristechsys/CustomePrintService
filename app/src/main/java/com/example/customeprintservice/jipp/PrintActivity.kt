package com.example.customeprintservice.jipp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.customeprintservice.R
import kotlinx.android.synthetic.main.activity_print.*

class PrintActivity : AppCompatActivity() {

    val printUtils = PrintUtils()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print)
        val actionBar = supportActionBar
        actionBar?.title = "Print"
        actionBar?.setDisplayHomeAsUpEnabled(true)

        btnPrint.setOnClickListener {
//            printUtils.print(uri1, file, this@PrintActivity)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}