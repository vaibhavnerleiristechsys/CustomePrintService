package com.printerlogic.printerlogic.printjobstatus.model.canceljob

import com.google.gson.annotations.SerializedName

data class QueuedPrintJobsCanceledItem(

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("id")
	val id: Int? = null
)