package net.ccbluex.liquidbounce.chat.packet

import com.google.gson.*
import me.liuli.elixir.utils.set
import net.ccbluex.liquidbounce.chat.packet.packets.Packet
import net.ccbluex.liquidbounce.file.FileManager.PRETTY_GSON
import java.lang.reflect.Type

class PacketDeserializer : JsonDeserializer<Packet> {

    private val packetRegistry = hashMapOf<String, Class<out Packet>>()

        fun registerPacket(packetName: String, packetClass: Class<out Packet>) {
        packetRegistry[packetName] = packetClass
    }

        override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext?): Packet? {
        // TODO: Use SerializedPacket class

        val packetObject = json.asJsonObject
        val packetName = packetObject["m"].asString

        if (packetName !in packetRegistry) return null

        if (!packetObject.has("c")) packetObject["c"] = JsonObject()

        return PRETTY_GSON.fromJson(packetObject["c"], packetRegistry[packetName])

    }

}