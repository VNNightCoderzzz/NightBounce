package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.io.MiscUtils

object UsernameCommand : Command("username", "ign") {
        override fun execute(args: Array<String>) {
        val username = mc.thePlayer.name

        chat("Username: $username")

        MiscUtils.copy(username)
    }
}