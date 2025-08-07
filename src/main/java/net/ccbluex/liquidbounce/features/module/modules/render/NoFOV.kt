package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module

object NoFOV : Module("NoFOV", Category.RENDER, gameDetecting = false) {
    val fov by float("FOV", 1f, 0f..1.5f)
}
