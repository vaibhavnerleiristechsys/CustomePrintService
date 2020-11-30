package com.example.customeprintservice.printjobstatus.model.getjobstatuses

import com.google.gson.annotations.SerializedName

data class Meta(

	@field:SerializedName("pagination")
	val pagination: Pagination? = null
)