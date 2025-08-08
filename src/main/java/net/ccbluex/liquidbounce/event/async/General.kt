package net.ccbluex.liquidbounce.event.async

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


suspend inline fun waitUntil(
    crossinline condition: () -> Boolean
): Int = suspendCancellableCoroutine { cont ->
    var waitingTick = 0
    TickScheduler.schedule {
        waitingTick++
        try {
            if (condition()) {
                cont.resume(waitingTick)
                true
            } else {
                false
            }
        } catch (e: Throwable) {
            cont.resumeWithException(e)
            true
        }
    }
}

suspend fun waitTicks(ticks: Int) {
    require(ticks >= 0) { "Negative tick: $ticks" }

    if (ticks == 0) {
        return
    }

    var remainingTick = ticks
    waitUntil { --remainingTick == 0 }
}

suspend inline fun waitConditional(
    ticks: Int,
    crossinline callback: (elapsedTicks: Int) -> Boolean
): Boolean {
    require(ticks >= 0) { "Negative tick: $ticks" }

    if (ticks == 0) {
        return true
    }

    var elapsedTicks = 0
    // `elapsedTicks` in 0 until `ticks`
    waitUntil { elapsedTicks >= ticks || callback(elapsedTicks++) }

    return elapsedTicks >= ticks
}
