package net.ccbluex.liquidbounce.features.module.modules.world

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object FastPlace : Module("FastPlace", Category.WORLD) {
    val speed by int("Speed", 0, 0..4)
    val onlyBlocks by boolean("OnlyBlocks", true)
    val facingBlocks by boolean("OnlyWhenFacingBlocks", true)
}