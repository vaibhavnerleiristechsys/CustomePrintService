package com.example.customeprintservice.jipp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log


class MyReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val ippPacket: String = intent.getStringExtra("getMessage").toString()
        Log.i("printer", "msg---->$ippPacket")

        try {
            val mainActivity = MainActivity()
//            mainActivity.getInst()?.updatedUi(ippPacket)
        } catch (e: Exception) { }
    }
}