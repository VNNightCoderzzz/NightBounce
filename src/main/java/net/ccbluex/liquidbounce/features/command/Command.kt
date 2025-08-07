package net.ccbluex.liquidbounce.features.command

import net.ccbluex.liquidbounce.LiquidBounce.commandManager
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.asResourceLocation
import net.ccbluex.liquidbounce.utils.client.playSound

abstract class Command(val command: String, vararg val alias: String) : MinecraftInstance {
        abstract fun execute(args: Array<String>)

        open fun tabComplete(args: Array<String>) = emptyList<String>()

        protected fun chat(msg: String) = net.ccbluex.liquidbounce.utils.client.chat("§3$msg")

        protected fun chatSyntax(syntax: String) = chat("§3Syntax: §7${commandManager.prefix}$syntax")

        protected fun chatSyntax(syntaxes: Array<String>) {
        chat("§3Syntax:")

        for (syntax in syntaxes)
            chat("§8> §7${commandManager.prefix}$command ${syntax.lowercase()}")
    }

        protected fun chatSyntaxError() = chat("§3Syntax error")

        protected fun playEdit() {
        mc.playSound("random.anvil_use".asResourceLocation())
    }
}