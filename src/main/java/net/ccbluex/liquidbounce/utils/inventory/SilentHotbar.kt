@file:Suppress("unused")

package net.ccbluex.liquidbounce.utils.inventory

import net.ccbluex.liquidbounce.event.ClientSlotChangeEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.minecraft.network.play.client.C09PacketHeldItemChange

object SilentHotbar : Listenable, MinecraftInstance {

    var hotbarState: SilentHotbarState? = null

    private var ticksSinceLastUpdate: Int = 0
    private var originalSlot: Int? = null

    val currentSlot: Int
        get() = hotbarState?.enforcedSlot ?: mc.thePlayer?.inventory?.currentItem ?: 0

    val modifiedThisTick
        get() = ticksSinceLastUpdate == 0 && hotbarState != null

    var ignoreSlotChange = false
    var pressedAtSlot = false

        fun selectSlotSilently(
        requester: Any?, slot: Int, ticksUntilReset: Int? = null, immediate: Boolean = false,
        render: Boolean = true, resetManually: Boolean = false,
    ) {
        if (originalSlot == null) {
            originalSlot = mc.thePlayer?.inventory?.currentItem ?: 0
        }

        hotbarState = SilentHotbarState(slot, requester, ticksUntilReset, render, resetManually)
        ticksSinceLastUpdate = 0

        if (immediate) {
            mc.playerController?.syncCurrentPlayItem()
        }
    }

    fun resetSlot(requester: Any? = null, immediate: Boolean = false) {
        val state = hotbarState ?: return

        if (requester == null || state.requester == requester) {
            hotbarState = null
            originalSlot = null

            if (requester != null && immediate) {
                mc.playerController?.syncCurrentPlayItem()
            }
        }
    }

        fun isSlotModified(requester: Any?) = hotbarState?.requester == requester

    fun updateSilentSlot() {
        pressedAtSlot = false

        val hotbarState = hotbarState ?: return

        hotbarState.resetTicks?.let { ticksUntilReset ->
            if (ticksSinceLastUpdate >= ticksUntilReset) {
                resetSlot(hotbarState.requester)
                return
            }
        }

        ticksSinceLastUpdate++
    }

    fun renderSlot(option: Boolean): Int {
        val player = mc.thePlayer ?: return 0

        val original = player.inventory.currentItem

        val state = hotbarState ?: return original

        return if (option || state.render) currentSlot else original
    }

    private fun shouldReset(slot: Int, other: Int? = originalSlot, keyPressCheck: Boolean = false): Boolean {
        return (slot != other || keyPressCheck) && hotbarState?.resetManually == true
    }

    val onSlotChange = handler<ClientSlotChangeEvent> { event ->
                if (ignoreSlotChange) {
            event.modifiedSlot = event.supposedSlot

            originalSlot = null
            ignoreSlotChange = false
            return@handler
        }

                if (originalSlot != null && shouldReset(event.supposedSlot, originalSlot, pressedAtSlot)) {
            resetSlot()

            event.modifiedSlot = event.supposedSlot
        }
    }

    val onPacket = handler<PacketEvent> { event ->
        val packet = event.packet

        if (packet !is C09PacketHeldItemChange)
            return@handler

                if (shouldReset(packet.slotId, currentSlot))
            resetSlot()
    }

    override fun handleEvents() = mc.thePlayer != null && mc.theWorld != null

}

class SilentHotbarState(
    val enforcedSlot: Int, var requester: Any?, var resetTicks: Int?,
    val render: Boolean, val resetManually: Boolean,
)
