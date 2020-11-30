package com.example.customeprintservice.printjobstatus.model.releasejob

import com.google.gson.annotations.SerializedName

data class ReleaseJobRequest(

	@field:SerializedName("release_jobs")
	var releaseJobs: List<ReleaseJobsItem?>? = null
)