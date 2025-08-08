package net.ccbluex.liquidbounce.event

abstract class Event

abstract class CancellableEvent : Event() {

        var isCancelled = false
    
        fun cancelEvent() {
        isCancelled = true
    }

}

enum class EventState(val stateName: String) {
    PRE("PRE"), POST("POST"), // MotionEvent
    SEND("SEND"), RECEIVE("RECEIVE") // PacketEvent
}