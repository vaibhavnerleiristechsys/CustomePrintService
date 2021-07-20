package com.printerlogic.printerlogic.printjobstatus.model.printerdetails

import com.fasterxml.jackson.annotation.JsonProperty

data class PrinterDetailsResponse(

	@field:JsonProperty("data")
	val data: Data? = null
)