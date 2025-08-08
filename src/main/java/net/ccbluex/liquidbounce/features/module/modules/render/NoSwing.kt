package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object NoSwing : Module("NoSwing", Category.RENDER) {
    val serverSide by boolean("ServerSide", true)
}