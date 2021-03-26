package com.example.customeprintservice.printjobstatus.model.printerdetails

import com.fasterxml.jackson.annotation.JsonProperty

data class Attributes(

	@field:JsonProperty("console_username")
	val consoleUsername: String? = null,

	@field:JsonProperty("global_disable_single_sign_on")
	val globalDisableSingleSignOn: Int? = null,

	@field:JsonProperty("is_color")
	val is_color: Int? = null,

	@field:JsonProperty("console_secret")
	val consoleSecret: String? = null,

	@field:JsonProperty("fast_release_type")
	val fastReleaseType: Int? = null,

	@field:JsonProperty("cpa_hp_needs_update")
	val cpaHpNeedsUpdate: Int? = null,

	@field:JsonProperty("snmp_community")
	val snmpCommunity: String? = null,

	@field:JsonProperty("protocol")
	val protocol: Int? = null,

	@field:JsonProperty("chromebook_printing")
	val chromebookPrinting: Int? = null,

	@field:JsonProperty("is_snmp_enabled")
	val isSnmpEnabled: Int? = null,

	@field:JsonProperty("disable_single_sign_on")
	val disableSingleSignOn: Int? = null,

	@field:JsonProperty("model")
	val model: String? = null,

	@field:JsonProperty("id")
	val id: Int? = null,

	@field:JsonProperty("console_password")
	val consolePassword: Any? = null,

	@field:JsonProperty("last_modified")
	val lastModified: String? = null,

	@field:JsonProperty("job_cost_template")
	val jobCostTemplate: Int? = null,

	@field:JsonProperty("host_address")
	val host_address: String? = null,

	@field:JsonProperty("host-address")
	val hostaddress: String? = null,

	@field:JsonProperty("direct_email_address")
	val directEmailAddress: String? = null,

	@field:JsonProperty("fast_release_enabled")
	val fastReleaseEnabled: Int? = null,

	@field:JsonProperty("cpa_hp_update_error")
	val cpaHpUpdateError: String? = null,

	@field:JsonProperty("hp_reader_badge_type")
	val hpReaderBadgeType: String? = null,

	@field:JsonProperty("distribute_always")
	val distributeAlways: Int? = null,

	@field:JsonProperty("console_attempted")
	val consoleAttempted: String? = null,

	@field:JsonProperty("profiling_flags")
	val profilingFlags: Int? = null,

	@field:JsonProperty("revision")
	val revision: Int? = null,

	@field:JsonProperty("is_pull_printer")
	val is_pull_printer: Int? = null,

	@field:JsonProperty("fast_release_device")
	val fastReleaseDevice: Int? = null,

	@field:JsonProperty("snmp_deviceindex")
	val snmpDeviceindex: String? = null,

	@field:JsonProperty("source_flags")
	val sourceFlags: Int? = null,

	@field:JsonProperty("pull_print")
	val pullPrint: Int? = null,

	@field:JsonProperty("console_version")
	val consoleVersion: String? = null,

	@field:JsonProperty("filter_exe")
	val filterExe: String? = null,

	@field:JsonProperty("container_id")
	val containerId: Int? = null,

	@field:JsonProperty("ip_search")
	val ipSearch: String? = null,

	@field:JsonProperty("global_console_printing")
	val globalConsolePrinting: Int? = null,

	@field:JsonProperty("console_installed")
	val consoleInstalled: Int? = null,

	@field:JsonProperty("cpa_hp_updated_at")
	val cpaHpUpdatedAt: String? = null,

	@field:JsonProperty("direct_email_value")
	val directEmailValue: Int? = null,

	@field:JsonProperty("title")
	val title: String? = null,

	@field:JsonProperty("snmp_community_private")
	val snmpCommunityPrivate: String? = null,

	@field:JsonProperty("original_source_description")
	val originalSourceDescription: String? = null,

	@field:JsonProperty("has_error")
	val hasError: Int? = null,

	@field:JsonProperty("is_byte_counting")
	val isByteCounting: Int? = null,

	@field:JsonProperty("mobile_print_email_address")
	val mobilePrintEmailAddress: String? = null,

	@field:JsonProperty("make")
	val make: String? = null,

	@field:JsonProperty("direct_email_guest_value")
	val directEmailGuestValue: Int? = null,

	@field:JsonProperty("supports_duplex")
	val supportsDuplex: Int? = null,

	@field:JsonProperty("last_snmp_attempt")
	val lastSnmpAttempt: String? = null,

	@field:JsonProperty("mobile_printing")
	val mobilePrinting: Int? = null,

	@field:JsonProperty("original_pid")
	val originalPid: String? = null,

	@field:JsonProperty("console_printing")
	val consolePrinting: Int? = null,

	@field:JsonProperty("secure_release")
	val secure_release: Int? = null,

	@field:JsonProperty("omplus_enabled")
	val omplusEnabled: Int? = null,

	@field:JsonProperty("queue_name")
	val queueName: String? = null,

	@field:JsonProperty("cpa_subtype")
	val cpaSubtype: String? = null,

	@field:JsonProperty("account_id")
	val accountId: Int? = null,

	@field:JsonProperty("mobile_print_enabled")
	val mobilePrintEnabled: Int? = null,

	@field:JsonProperty("badge_scan")
	val badgeScan: Int? = null,

	@field:JsonProperty("port_number")
	val port_number: Int? = null,

	@field:JsonProperty("location")
	val location: String? = null,

	@field:JsonProperty("comment")
	val comment: String? = null,

	@field:JsonProperty("badge_scan_installed")
	val badgeScanInstalled: Int? = null
)