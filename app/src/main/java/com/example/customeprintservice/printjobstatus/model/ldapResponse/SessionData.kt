package com.example.customeprintservice.printjobstatus.model.ldapResponse

import com.example.customeprintservice.printjobstatus.model.printerdetails.Attributes
import com.fasterxml.jackson.annotation.JsonProperty


data class SessionData(


    @field:JsonProperty("id")
    val id: String? = null,

    @field:JsonProperty("type")
    val type: String? = null

)