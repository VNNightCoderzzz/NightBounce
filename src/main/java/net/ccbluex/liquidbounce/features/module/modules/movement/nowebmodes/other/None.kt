package net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.other

import net.ccbluex.liquidbounce.features.module.modules.movement.nowebmodes.NoWebMode

object None : NoWebMode("None") {
    override fun onUpdate() {
        mc.thePlayer.isInWeb = false
    }
}
