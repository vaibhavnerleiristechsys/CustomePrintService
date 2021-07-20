package com.printerlogic.printerlogic.printjobstatus.model.getjobstatuses

import com.google.gson.annotations.SerializedName

data class Meta(

	@field:SerializedName("pagination")
	val pagination: Pagination? = null
)