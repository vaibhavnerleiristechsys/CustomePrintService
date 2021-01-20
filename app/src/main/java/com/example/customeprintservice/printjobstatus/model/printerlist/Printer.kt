package com.example.customeprintservice.printjobstatus.model.printerlist

import com.fasterxml.jackson.annotation.JsonProperty

data class Printer (

    @field:JsonProperty("id")
    val id: String? = null,

    @field:JsonProperty("parent_id")
    val parent_id: String? = null,

    @field:JsonProperty("node_title")
    val node_title: String? = null

)