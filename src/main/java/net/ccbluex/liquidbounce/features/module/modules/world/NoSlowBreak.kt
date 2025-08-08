package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object NoSlowBreak : Module("NoSlowBreak", Category.WORLD, gameDetecting = false) {
    val air by boolean("Air", true)
    val water by boolean("Water", false)
}
