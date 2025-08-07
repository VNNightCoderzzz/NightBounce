package net.ccbluex.liquidbounce.chat.packet

import com.google.gson.JsonElement
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import net.ccbluex.liquidbounce.chat.packet.packets.Packet
import net.ccbluex.liquidbounce.file.FileManager.PRETTY_GSON
import java.lang.reflect.Type

class PacketSerializer : JsonSerializer<Packet> {

    private val packetRegistry = hashMapOf<Class<out Packet>, String>()

        fun registerPacket(packetName: String, packetClass: Class<out Packet>) {
        packetRegistry[packetClass] = packetName
    }

        override fun serialize(src: Packet, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        val packetName = packetRegistry.getOrDefault(src.javaClass, "UNKNOWN")
        val serializedPacket = SerializedPacket(packetName, if (src.javaClass.constructors.none { it.parameterCount != 0 }) null else src )

        return PRETTY_GSON.toJsonTree(serializedPacket)
    }

}