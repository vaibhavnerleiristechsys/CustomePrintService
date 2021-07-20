package com.printerlogic.printerlogic.printjobstatus.model.ldapResponse

import com.fasterxml.jackson.annotation.JsonProperty


data class SessionData(


    @field:JsonProperty("id")
    val id: String? = null,

    @field:JsonProperty("type")
    val type: String? = null

)