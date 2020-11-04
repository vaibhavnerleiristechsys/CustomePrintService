package com.example.customeprintservice.printjobstatus.model.jobstatus

import com.google.gson.annotations.SerializedName


data class JobStatusCanceledResponse(

	@field:SerializedName("queuedPrintJobsCanceled")
	val queuedPrintJobsCanceled: List<QueuedPrintJobsCanceledItem?>? = null
){
	override fun toString(): String {
		return "JobStatusCanceledResponse(queuedPrintJobsCanceled=$queuedPrintJobsCanceled)"
	}
}