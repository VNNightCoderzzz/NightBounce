package net.ccbluex.liquidbounce.script.api.global

import net.ccbluex.liquidbounce.utils.inventory.ItemUtils
import net.minecraft.item.ItemStack

object Item {

        @JvmStatic
    fun create(itemArguments: String): ItemStack? = ItemUtils.createItem(itemArguments)

}