package com.example.customeprintservice.printjobstatus.model.releasejob

import com.google.gson.annotations.SerializedName

data class QueuedPrintJobsReleasedItem(

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("id")
	val id: Int? = null
)