package com.example.customeprintservice.printjobstatus.model.jobstatus

import com.google.gson.annotations.SerializedName

data class DeleteJobsItem(

	@field:SerializedName("job_type")
	var jobType: Int? = null,

	@field:SerializedName("job_num")
	var jobNum: Int? = null,

	@field:SerializedName("user_name")
	var userName: String? = null,

	@field:SerializedName("workstation_id")
	var workstationId: Int? = null,

	@field:SerializedName("queue_id")
	var queueId: Int? = null
)