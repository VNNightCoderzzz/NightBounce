package net.ccbluex.liquidbounce.api

import com.google.gson.annotations.SerializedName


data class Build(
    @SerializedName("build_id")
    val buildId: Int,
    @SerializedName("commit_id")
    val commitId: String,
    val branch: String,
    @SerializedName("lb_version")
    val lbVersion: String,
    @SerializedName("mc_version")
    val mcVersion: String,
    val release: Boolean,
    val date: String,
    val message: String,
    val url: String
)

data class AutoSettings(
    @SerializedName("setting_id")
    val settingId: String,
    val name: String,
    @SerializedName("setting_type")
    val type: AutoSettingsType,
    val description: String,
    var date: String,
    val contributors: String,
    @SerializedName("status_type")
    val statusType: AutoSettingsStatusType,
    @SerializedName("status_date")
    var statusDate: String
)

enum class AutoSettingsType(val displayName: String) {
    @SerializedName("Rage")
    RAGE("Rage"),

    @SerializedName("Legit")
    LEGIT("Legit")
}

enum class AutoSettingsStatusType(val displayName: String) {
    @SerializedName("NotBypassing")
    NOT_BYPASSING("Not Bypassing"),

    @SerializedName("Bypassing")
    BYPASSING("Bypassing"),

    @SerializedName("Undetectable")
    UNDETECTABLE("Undetectable"),

    @SerializedName("Unknown")
    UNKNOWN("Unknown")
}

data class UploadResponse(val status: Status, val message: String, val token: String)

data class ReportResponse(val status: Status, val message: String)

enum class Status {
    @SerializedName("success")
    SUCCESS,

    @SerializedName("error")
    ERROR
}
