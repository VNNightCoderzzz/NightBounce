package net.ccbluex.liquidbounce.tabs

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import net.minecraft.item.ItemStack

class BlocksTab : CreativeTabs("Special blocks") {

    private val itemStacks by lazy(LazyThreadSafetyMode.NONE) {
        arrayOf(
            ItemStack(Blocks.command_block),
            ItemStack(Items.command_block_minecart),
            ItemStack(Blocks.barrier),
            ItemStack(Blocks.dragon_egg),
            ItemStack(Blocks.brown_mushroom_block),
            ItemStack(Blocks.red_mushroom_block),
            ItemStack(Blocks.farmland),
            ItemStack(Blocks.mob_spawner),
            ItemStack(Blocks.lit_furnace)
        )
    }

        init {
        backgroundImageName = "item_search.png"
    }

        override fun displayAllReleventItems(itemList: MutableList<ItemStack>) {
        itemList += itemStacks
    }

        override fun getTabIconItem(): Item = ItemStack(Blocks.command_block).item

        override fun getTranslatedTabLabel() = "Special blocks"

        override fun hasSearchBar() = true
}