package net.ccbluex.liquidbounce.event.async

import kotlinx.coroutines.Dispatchers
import net.ccbluex.liquidbounce.event.GameTickEvent
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.PacketEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import java.util.function.BooleanSupplier
import kotlin.coroutines.RestrictsSuspension

object TickScheduler : Listenable, MinecraftInstance {

    private val currentTickTasks = arrayListOf<BooleanSupplier>()
    private val nextTickTasks = arrayListOf<BooleanSupplier>()

    init {
        handler<GameTickEvent>(priority = Byte.MAX_VALUE) {
            currentTickTasks.removeIf { it.asBoolean }
            currentTickTasks += nextTickTasks
            nextTickTasks.clear()
        }
    }

        fun schedule(breakLoop: BooleanSupplier) {
        // Prevent modification in removeIf (Continuation.resume)
        mc.addScheduledTask { nextTickTasks += breakLoop }
    }
}
