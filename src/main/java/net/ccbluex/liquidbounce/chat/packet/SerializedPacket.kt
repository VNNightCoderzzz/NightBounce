package net.ccbluex.liquidbounce.chat.packet

import com.google.gson.annotations.SerializedName
import net.ccbluex.liquidbounce.chat.packet.packets.Packet

data class SerializedPacket(
    @SerializedName("m")
    val packetName: String,

    @SerializedName("c")
    val packetContent: Packet?
)