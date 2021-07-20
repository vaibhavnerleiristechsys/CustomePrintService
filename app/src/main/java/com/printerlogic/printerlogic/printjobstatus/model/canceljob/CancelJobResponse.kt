package com.printerlogic.printerlogic.printjobstatus.model.canceljob

import com.google.gson.annotations.SerializedName

data class CancelJobResponse(

	@field:SerializedName("queuedPrintJobsCanceled")
	val queuedPrintJobsCanceled: List<QueuedPrintJobsCanceledItem?>? = null
)