package com.example.customeprintservice.printjobstatus.model.printerdetails

import com.fasterxml.jackson.annotation.JsonProperty

data class PrinterDetailsResponse(

	@field:JsonProperty("data")
	val data: Data? = null
)