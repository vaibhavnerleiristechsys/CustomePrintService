package com.example.customeprintservice.jipp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.customeprintservice.R

class PrintActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_print)
        val actionBar = supportActionBar
        actionBar?.title = "Print"
        actionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}