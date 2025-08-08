package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object SilentHotbarModule : Module("SilentHotbar", Category.RENDER) {
    val keepHighlightedName by boolean("KeepHighlightedName", false)
    val keepHotbarSlot by boolean("KeepHotbarSlot", false)
    val keepItemInHandInFirstPerson by boolean("KeepItemInHandInFirstPerson", false)
    val keepItemInHandInThirdPerson by boolean("KeepItemInHandInThirdPerson", false)
}