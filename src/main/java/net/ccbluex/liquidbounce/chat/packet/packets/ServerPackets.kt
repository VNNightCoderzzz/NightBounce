package net.ccbluex.liquidbounce.chat.packet.packets

import com.google.gson.annotations.SerializedName
import java.util.*


class ServerRequestMojangInfoPacket : Packet

data class ServerLoginMojangPacket(

        @SerializedName("name")
        val name: String,

        @SerializedName("uuid")
        val uuid: UUID,

        @SerializedName("allow_messages")
        val allowMessages: Boolean

) : Packet


data class ServerLoginJWTPacket(

        @SerializedName("token")
        val token: String,

        @SerializedName("allow_messages")
        val allowMessages: Boolean

) : Packet

data class ServerMessagePacket(

        @SerializedName("content")
        val content: String

) : Packet

data class ServerPrivateMessagePacket(

        @SerializedName("receiver")
        val receiver: String,

        @SerializedName("content")
        val content: String

) : Packet

data class ServerBanUserPacket(

        @SerializedName("user")
        val user: String

) : Packet

data class ServerUnbanUserPacket(

        @SerializedName("user")
        val user: String

) : Packet

class ServerRequestJWTPacket : Packet