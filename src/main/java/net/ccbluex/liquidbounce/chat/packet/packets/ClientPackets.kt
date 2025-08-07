package net.ccbluex.liquidbounce.chat.packet.packets

import com.google.gson.annotations.SerializedName
import net.ccbluex.liquidbounce.chat.User


data class ClientMojangInfoPacket(

        @SerializedName("session_hash")
        val sessionHash: String

) : Packet

data class ClientNewJWTPacket(

        @SerializedName("token")
        val token: String

) : Packet

data class ClientMessagePacket(

        @SerializedName("author_id")
        val id: String,

        @SerializedName("author_info")
        val user: User,

        @SerializedName("content")
        val content: String

) : Packet

data class ClientPrivateMessagePacket(

        @SerializedName("author_id")
        val id: String,

        @SerializedName("author_info")
        val user: User,

        @SerializedName("content")
        val content: String

) : Packet

data class ClientSuccessPacket(

        @SerializedName("reason")
        val reason: String

) : Packet

data class ClientErrorPacket(

        @SerializedName("message")
        val message: String

) : Packet