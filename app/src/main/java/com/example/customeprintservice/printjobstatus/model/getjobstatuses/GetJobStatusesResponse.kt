package com.example.customeprintservice.printjobstatus.model.getjobstatuses

import com.google.gson.annotations.SerializedName

data class GetJobStatusesResponse(

	@field:SerializedName("printQueueJobStatus")
	val printQueueJobStatus: List<PrintQueueJobStatusItem?>? = null,

	@field:SerializedName("meta")
	val meta: Meta? = null
)