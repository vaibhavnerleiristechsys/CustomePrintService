package com.example.customeprintservice.printjobstatus.model

import com.fasterxml.jackson.annotation.JsonProperty

data class PrintQueueJobStatusItem(

	@field:JsonProperty("createdByUserId")
	val createdByUserId: Any? = null,

	@field:JsonProperty("submittedAtRelative")
	val submittedAtRelative: String? = null,

	@field:JsonProperty("uploadId")
	val uploadId: Int? = null,

	@field:JsonProperty("displayOrder")
	val displayOrder: Int? = null,

	@field:JsonProperty("userName")
	val userName: String? = null,

	@field:JsonProperty("queuedAt")
	val queuedAt: String? = null,

	@field:JsonProperty("printerDeviceQueueId")
	val printerDeviceQueueId: Int? = null,

	@field:JsonProperty("statusDescription")
	val statusDescription: Any? = null,

	@field:JsonProperty("pages")
	val pages: Int? = null,

	@field:JsonProperty("jobSize")
	val jobSize: Int? = null,

	@field:JsonProperty("jobSourceType")
	val jobSourceType: String? = null,

	@field:JsonProperty("workstationId")
	val workstationId: Int? = null,

	@field:JsonProperty("sourceMachine")
	val sourceMachine: String? = null,

	@field:JsonProperty("id")
	val id: String? = null,

	@field:JsonProperty("documentTitle")
	val documentTitle: String? = null,

	@field:JsonProperty("submittedAt")
	val submittedAt: String? = null,

	@field:JsonProperty("jobType")
	val jobType: String? = null,

	@field:JsonProperty("jobNumber")
	val jobNumber: Int? = null,

	@field:JsonProperty("status")
	val status: Int? = null
)