package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce.moduleManager
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.ui.client.hud.HUD.addNotification
import net.ccbluex.liquidbounce.ui.client.hud.element.elements.Notification
import org.lwjgl.input.Keyboard

object BestBindSetupCommand : Command("bestbindsetup") {

    override fun execute(args: Array<String>) {
        moduleManager["KillAura"]?.let {
            it.keyBind = Keyboard.getKeyIndex("R")
        }
        moduleManager["Scaffold"]?.let {
            it.keyBind = Keyboard.getKeyIndex("F")
        }
        chat("§a§lKill Aura Has Been Set To R, Scaffold Has Been Set To F")
        addNotification(Notification("Setup Success", "Kill Aura Has Been Set To R, Scaffold Has Been Set To F"))
        playEdit()

    override fun tabComplete(args: Array<String>): List<String> = emptyList()
}
