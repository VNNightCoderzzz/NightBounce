package net.ccbluex.liquidbounce.script.api

import jdk.nashorn.api.scripting.JSObject
import jdk.nashorn.api.scripting.ScriptUtils
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER

class ScriptCommand(private val commandObject: JSObject) : Command(commandObject.getMember("name") as String,
        *ScriptUtils.convert(commandObject.getMember("aliases"), Array<String>::class.java) as Array<out String>) {

    private val events = hashMapOf<String, JSObject>()

        fun on(eventName: String, handler: JSObject) {
        events[eventName] = handler
    }

    override fun execute(args: Array<String>) {
        try {
            events["execute"]?.call(commandObject, args)
        } catch (throwable: Throwable) {
            LOGGER.error("[ScriptAPI] Exception in command '$command'!", throwable)
        }
    }
}