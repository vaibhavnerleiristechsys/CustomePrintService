package com.example.customeprintservice.printjobstatus.model

import com.fasterxml.jackson.annotation.JsonProperty

data class Pagination(

	@field:JsonProperty("per_page")
	val perPage: Int? = null,

	@field:JsonProperty("total")
	val total: Int? = null,

	@field:JsonProperty("count")
	val count: Int? = null,

	@field:JsonProperty("links")
	val links: List<Any?>? = null,

	@field:JsonProperty("total_pages")
	val totalPages: Int? = null,

	@field:JsonProperty("current_page")
	val currentPage: Int? = null
)