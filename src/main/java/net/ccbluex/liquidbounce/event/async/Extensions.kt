package net.ccbluex.liquidbounce.event.async

import kotlinx.coroutines.*
import net.ccbluex.liquidbounce.event.*


fun Listenable.launchSequence(
    dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    always: Boolean = false,
    body: suspend CoroutineScope.() -> Unit
) {
    val job = EventManager.launch(dispatcher, block = body)

    TickScheduler.schedule {
        if (!always && !this@launchSequence.handleEvents()) {
            job.cancel()
            true
        } else {
            job.isCompleted
        }
    }
}

fun Listenable.loopSequence(
    dispatcher: CoroutineDispatcher = Dispatchers.Unconfined,
    always: Boolean = false,
    priority: Byte = 0,
    body: suspend CoroutineScope.() -> Unit
) {
    var job = EventManager.launch(dispatcher, block = body)

    handler<GameTickEvent>(always = true, priority) {
        if (!always && !this@loopSequence.handleEvents()) {
            job.cancel()
        } else if (!job.isActive) {
            job = EventManager.launch(dispatcher, block = body)
        }
    }
}
