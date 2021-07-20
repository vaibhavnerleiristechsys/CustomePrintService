package com.printerlogic.printerlogic.printjobstatus.model.ldapResponse

import com.fasterxml.jackson.annotation.JsonProperty


data class LdapUserNameResponse(

    @field:JsonProperty("meta")
    val meta: SessionMeta? = null
)