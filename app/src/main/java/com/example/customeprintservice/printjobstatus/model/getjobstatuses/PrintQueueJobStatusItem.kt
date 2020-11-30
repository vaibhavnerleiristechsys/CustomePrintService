package com.example.customeprintservice.printjobstatus.model.getjobstatuses

import com.google.gson.annotations.SerializedName

data class PrintQueueJobStatusItem(

	@field:SerializedName("createdByUserId")
	val createdByUserId: Any? = null,

	@field:SerializedName("submittedAtRelative")
	val submittedAtRelative: String? = null,

	@field:SerializedName("uploadId")
	val uploadId: Int? = null,

	@field:SerializedName("displayOrder")
	val displayOrder: Int? = null,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("queuedAt")
	val queuedAt: String? = null,

	@field:SerializedName("printerDeviceQueueId")
	val printerDeviceQueueId: Int? = null,

	@field:SerializedName("statusDescription")
	val statusDescription: Any? = null,

	@field:SerializedName("pages")
	val pages: Int? = null,

	@field:SerializedName("jobSize")
	val jobSize: Int? = null,

	@field:SerializedName("jobSourceType")
	val jobSourceType: String? = null,

	@field:SerializedName("workstationId")
	val workstationId: Int? = null,

	@field:SerializedName("sourceMachine")
	val sourceMachine: String? = null,

	@field:SerializedName("id")
	val id: String? = null,

	@field:SerializedName("documentTitle")
	val documentTitle: String? = null,

	@field:SerializedName("submittedAt")
	val submittedAt: String? = null,

	@field:SerializedName("jobType")
	val jobType: String? = null,

	@field:SerializedName("jobNumber")
	val jobNumber: Int? = null,

	@field:SerializedName("status")
	val status: Int? = null
)