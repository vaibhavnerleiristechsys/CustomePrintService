package com.example.customeprintservice.printjobstatus.model.jobstatus

import com.google.gson.annotations.SerializedName

data class JobStatusCancel(

	@field:SerializedName("delete_jobs")
	var deleteJobs: List<DeleteJobsItem?>? = null
)