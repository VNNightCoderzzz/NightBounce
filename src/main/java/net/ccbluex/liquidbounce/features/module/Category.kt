package net.ccbluex.liquidbounce.features.module

import net.ccbluex.liquidbounce.LiquidBounce.CLIENT_NAME
import net.minecraft.util.ResourceLocation

enum class Category(val displayName: String) {

    COMBAT("Combat"),
    PLAYER("Player"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    WORLD("World"),
    MISC("Misc"),
    EXPLOIT("Exploit"),
    FUN("Fun");

    val iconResourceLocation = ResourceLocation("${CLIENT_NAME.lowercase()}/tabgui/${name.lowercase()}.png")

}
