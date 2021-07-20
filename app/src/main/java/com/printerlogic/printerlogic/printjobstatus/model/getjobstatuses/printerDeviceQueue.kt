package com.printerlogic.printerlogic.printjobstatus.model.getjobstatuses

import com.google.gson.annotations.SerializedName

data class printerDeviceQueue (

    @field:SerializedName("printers")
    val printers: List<printers?>? = null
)