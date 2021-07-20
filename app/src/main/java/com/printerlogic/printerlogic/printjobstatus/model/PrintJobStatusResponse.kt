package com.printerlogic.printerlogic.printjobstatus.model

import com.fasterxml.jackson.annotation.JsonProperty

data class PrintJobStatusResponse(

	@field:JsonProperty("printQueueJobStatus")
	val printQueueJobStatus: List<PrintQueueJobStatusItem?>? = null,

	@field:JsonProperty("meta")
	val meta: Meta? = null
)