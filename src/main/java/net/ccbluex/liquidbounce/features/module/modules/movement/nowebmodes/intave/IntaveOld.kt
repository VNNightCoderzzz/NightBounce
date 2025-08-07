package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.intave

import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.NoWebMode

object IntaveOld : NoWebMode("IntaveOld") {
    override fun onUpdate() {
        val thePlayer = mc.thePlayer ?: return

        if (!thePlayer.isInWeb) {
            return
        }

        if (thePlayer.movementInput.moveStrafe == 0.0F && mc.gameSettings.keyBindForward.isKeyDown && thePlayer.isCollidedVertically) {
            thePlayer.jumpMovementFactor = 0.74F
        } else {
            thePlayer.jumpMovementFactor = 0.2F
            thePlayer.onGround = true
        }
    }
}
