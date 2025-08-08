package net.ccbluex.liquidbounce.utils.attack

import net.ccbluex.liquidbounce.utils.client.ClientUtils.runTimeTicks

object CPSCounter {
    private const val MAX_CPS = 50
    private val TIMESTAMP_BUFFERS = Array(MouseButton.entries.size) { RollingArrayLongBuffer(MAX_CPS) }

        fun registerClick(button: MouseButton) = TIMESTAMP_BUFFERS[button.ordinal].add(runTimeTicks.toLong())

        fun getCPS(button: MouseButton, timeStampsSince: Int = runTimeTicks - 20) =
        TIMESTAMP_BUFFERS[button.ordinal].getTimestampsSince(timeStampsSince.toLong())

    enum class MouseButton { LEFT, MIDDLE, RIGHT }
}
