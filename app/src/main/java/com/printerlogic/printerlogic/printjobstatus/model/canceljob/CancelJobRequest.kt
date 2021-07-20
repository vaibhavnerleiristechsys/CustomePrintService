package com.printerlogic.printerlogic.printjobstatus.model.canceljob

import com.google.gson.annotations.SerializedName

data class CancelJobRequest(

	@field:SerializedName("delete_jobs")
	var deleteJobs: List<DeleteJobsItem?>? = null
)