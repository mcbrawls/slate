package net.mcbrawls.slate.test

import net.mcbrawls.slate.Slate
import net.mcbrawls.slate.tile.Tile
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

class TestSlate : Slate() {
    init {
        tileGrid[0] = Tile(ItemStack(Items.STONE))
    }
}
