package net.ccbluex.liquidbounce.chat

import net.ccbluex.liquidbounce.chat.packet.packets.Packet

interface ClientListener {

        fun onConnect()

        fun onConnected()

        fun onHandshake(success: Boolean)

        fun onDisconnect()

        fun onLogon()

        fun onPacket(packet: Packet)

        fun onError(cause: Throwable)

}