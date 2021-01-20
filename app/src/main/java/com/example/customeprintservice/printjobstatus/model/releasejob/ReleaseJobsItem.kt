package com.example.customeprintservice.printjobstatus.model.releasejob

import com.google.gson.annotations.SerializedName

data class ReleaseJobsItem(

	@field:SerializedName("job_type")
	var jobType: String? = null,

	@field:SerializedName("job_num")
	var jobNum: Int? = null,

	@field:SerializedName("user_name")
	var userName: String? = null,

	@field:SerializedName("workstation_id")
	var workstationId: Int? = null,

	@field:SerializedName("queue_id")
	var queueId: Int? = null
)
