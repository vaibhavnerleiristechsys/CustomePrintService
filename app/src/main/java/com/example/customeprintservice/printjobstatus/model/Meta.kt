package com.example.customeprintservice.printjobstatus.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Meta(

	@field:JsonProperty("pagination")
	val pagination: Pagination? = null
)