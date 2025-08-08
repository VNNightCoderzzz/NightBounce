package net.ccbluex.liquidbounce.utils.client

import net.ccbluex.liquidbounce.utils.attack.RollingArrayLongBuffer

object PPSCounter {
    private val TIMESTAMP_BUFFERS = Array(PacketType.entries.size) { RollingArrayLongBuffer(99999) }

        fun registerType(type: PacketType) = TIMESTAMP_BUFFERS[type.ordinal].add(System.currentTimeMillis())

        fun getPPS(type: PacketType) = TIMESTAMP_BUFFERS[type.ordinal].getTimestampsSince(System.currentTimeMillis() - 1000L)

    enum class PacketType { SEND, RECEIVED }
}
