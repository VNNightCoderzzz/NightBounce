package net.ccbluex.liquidbounce.script.api.global

import net.ccbluex.liquidbounce.utils.client.chat

object Chat {

        @Suppress("unused")
    @JvmStatic
    fun print(message : String) = chat(message)
}