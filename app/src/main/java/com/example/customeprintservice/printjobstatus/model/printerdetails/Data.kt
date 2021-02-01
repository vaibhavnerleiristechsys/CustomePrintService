package com.example.customeprintservice.printjobstatus.model.printerdetails

import com.fasterxml.jackson.annotation.JsonProperty

data class Data(

	@field:JsonProperty("attributes")
	val attributes: Attributes? = null,

	@field:JsonProperty("id")
	val id: Int? = null,

	@field:JsonProperty("type")
	val type: String? = null

)