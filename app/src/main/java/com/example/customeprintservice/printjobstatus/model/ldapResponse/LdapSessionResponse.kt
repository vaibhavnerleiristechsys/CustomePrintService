package com.example.customeprintservice.printjobstatus.model.ldapResponse

import com.fasterxml.jackson.annotation.JsonProperty

data class LdapSessionResponse(

    @field:JsonProperty("data")
    val data: SessionData? = null
)