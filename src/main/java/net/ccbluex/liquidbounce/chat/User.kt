package net.ccbluex.liquidbounce.chat

import com.google.gson.annotations.SerializedName
import java.util.*

data class User(
    @SerializedName("name")
    val name: String,

    @SerializedName("uuid")
    val uuid: UUID
)