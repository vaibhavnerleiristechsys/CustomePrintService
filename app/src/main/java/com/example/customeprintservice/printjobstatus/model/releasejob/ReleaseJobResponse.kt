package com.example.customeprintservice.printjobstatus.model.releasejob

import com.google.gson.annotations.SerializedName

data class ReleaseJobResponse(

	@field:SerializedName("queuedPrintJobsReleased")
	val queuedPrintJobsReleased: List<QueuedPrintJobsReleasedItem?>? = null
)